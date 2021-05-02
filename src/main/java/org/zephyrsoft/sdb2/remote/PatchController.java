package org.zephyrsoft.sdb2.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.FileAndDirectoryLocations;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.SongsModelController;
import org.zephyrsoft.sdb2.model.XMLConverter;

public class PatchController extends SongsModelController {
	
	private static final Logger LOG = LoggerFactory.getLogger(PatchController.class);
	
	private static final String PREF_COMMENT = "RemoteController DB Properties";
	private static final String PREF_DB_PREFIX = "DB_PREFIX";
	private static final String PREF_DB_SERVER = "DB_SERVER";
	private static final String PREF_SONGS_VERSION_ID = "SONGS_VERSION_ID";
	private final HashMap<String, Collection<Song>> patchMap = new HashMap<>();
	private final HashMap<Long, PatchVersion> patchVersions = new HashMap<>();
	private final HashSet<String> rejects = new HashSet<>();
	private long currentVersionId;
	private final RemoteController remoteController;
	private boolean initialRequestMissingPatches;
	private SongsModel db;
	private List<Song> offlineChanges;
	
	// Contains overwritten local changes: TODO: Add all rejected too
	// We could create a mergetool to
	private LinkedList<Song> conflicts = new LinkedList<>();
	
	public PatchController(SongsModel songs, RemoteController remoteController) {
		super(songs);
		this.remoteController = remoteController;
		
		load();
		
		remoteController.getLatestPatch().onChange((patch, args) -> addPatch(patch, (String) args[RemoteTopic.PATCHES_LATEST_PATCH_ARG_UUID]));
		remoteController.getRequestPatch().onChange((patch, args) -> addPatch(patch, (String) args[RemoteTopic.PATCHES_REQUEST_PATCH_ARG_UUID]));
		remoteController.getLatestVersion().onChange((version, args) -> addPatchVersion(version));
		remoteController.getRequestVersion().onChange((version, args) -> addPatchVersion(version));
		remoteController.getLatestReject().onChange((uuid, args) -> rejectPatch(uuid));
		
		addPatchVersion(remoteController.getLatestVersion().get());
	}
	
	/**
	 * Resets the database if prefix has changed, reads the current database version resets the
	 * requestMissingPatch boolean.
	 */
	private boolean load() {
		File dbFile = new File(FileAndDirectoryLocations.getDefaultDBFileName());
		File dbPropertiesFile = new File(FileAndDirectoryLocations.getDefaultDBPropertiesFileName());
		if (dbFile.exists() && !dbPropertiesFile.exists())
			dbFile.delete();
		if (!dbFile.exists() && dbPropertiesFile.exists())
			dbPropertiesFile.delete();
		
		// Load db & db properties:
		Properties properties = new Properties();
		if (!dbFile.exists()) {
			LOG.debug("not reading db from {} (file does not exist) but using empty model", dbFile.getAbsolutePath());
			db = new SongsModel();
		} else {
			LOG.debug("loading db from file {} and properties from file {}", dbFile.getAbsolutePath(), dbPropertiesFile.getAbsolutePath());
			try {
				db = XMLConverter.fromXMLToPersistable(new FileInputStream(dbFile));
				if (db == null)
					throw new IOException();
				properties.loadFromXML(new FileInputStream(dbPropertiesFile));
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error("Could not read db from file {} and properties from file {}", dbFile.getAbsolutePath(), dbPropertiesFile.getAbsolutePath());
				db = new SongsModel();
				properties.clear();
			}
		}
		
		// Reset if prefix has changed:
		currentVersionId = Long.parseLong(properties.getProperty(PREF_SONGS_VERSION_ID, "0"));
		if (!remoteController.getPrefix().equals(properties.getProperty(PREF_DB_PREFIX, remoteController.getPrefix())) ||
			!remoteController.getServer().equals(properties.getProperty(PREF_DB_SERVER, remoteController.getServer())))
			resetDB();
		LOG.debug("Loaded db for server " + remoteController.getServer() + "\\" + remoteController.getPrefix() + " and version: " + currentVersionId);
		
		// Collect offline changes, align songs with db, and rebase changes later:
		offlineChanges = collectChanges(songs);
		songs.update(db);
		
		return true;
	}
	
	/**
	 * To reject patches.
	 * 
	 * TODO: There is currently no reject handling for published patches by this client.
	 * As the server may allows fast forward merges. The user has to handle everything else.
	 * 
	 * @param hash
	 */
	private void rejectPatch(String hash) {
		if (patchMap.containsKey(hash))
			patchMap.remove(hash);
		else
			rejects.add(hash);
	}
	
	/**
	 * Add patchversion to patchversion list.
	 *
	 * @param patchVersion
	 */
	private void addPatchVersion(PatchVersion patchVersion) {
		if (patchVersion == null)
			return;
		
		if (patchVersion.getId() == currentVersionId) {
			publishOfflineChanges();
			return;
		}
		
		if (patchVersion.getId() > currentVersionId) {
			patchVersions.put(patchVersion.getId(), patchVersion);
			applyPatches();
			if (!initialRequestMissingPatches && remoteController.getHealthDB().get() == Health.online) {
				initialRequestMissingPatches = true;
				requestMissingPatches();
			}
		} else if (patchVersion.getId() < currentVersionId) {
			resetDB();
			requestMissingPatches();
		}
	}
	
	/**
	 * This will reset the local database to version 0.
	 * You have to request missing patches again to reset to the latest version.
	 */
	private void resetDB() {
		patchVersions.clear();
		rejects.clear();
		patchMap.clear();
		conflicts.clear();
		offlineChanges = null;
		
		db.clear();
		songs.clear();
		currentVersionId = 0;
	}
	
	/**
	 * Add a new patch to the patchMap and apply if the version exists and if it's not rejected.
	 *
	 * @param patch
	 */
	private void addPatch(SongsModel patch, String uuid) {
		if (rejects.contains(uuid))
			rejects.remove(uuid);
		else
			patchMap.put(uuid, patch.getSongs());
		applyPatches();
	}
	
	/**
	 * This will apply all recently received patches by updating the db and the main song list.
	 * It will run only if we got all patches up to the last one to not update the local database to frequently.
	 */
	private void applyPatches() {
		for (Long i = currentVersionId + 1; i <= remoteController.getLatestVersion().get().getId(); i++) {
			if (!patchVersions.containsKey(i) || !patchMap.containsKey(patchVersions.get(i).getUUID()))
				return;
		}
		
		ArrayList<Song> changedSongs = new ArrayList<>();
		long versionAfterChangingSongs = currentVersionId;
		final DiffMatchPatch dmp = new DiffMatchPatch();
		while (patchVersions.containsKey(versionAfterChangingSongs + 1) && patchMap.containsKey(patchVersions.get(versionAfterChangingSongs + 1)
			.getUUID())) {
			PatchVersion nextVersion = patchVersions.get(versionAfterChangingSongs + 1);
			Collection<Song> patch = patchMap.get(nextVersion.getUUID());
			for (Song patchSong : patch) {
				Song song = db.getByUUID(patchSong.getUUID());
				Map<String, String> songEntries = song == null ? new Song(patchSong.getUUID()).toMap() : song.toMap();
				for (Map.Entry<String, String> patchEntry : patchSong.toMap().entrySet()) {
					if (patchEntry.getKey().equals("uuid") || patchEntry.getValue() == null || patchEntry.getValue().isEmpty())
						continue;
					
					String previous = songEntries.containsKey(patchEntry.getKey()) && songEntries.get(patchEntry.getKey()) != null ? songEntries
						.get(patchEntry.getKey()) : "";
					LinkedList<Patch> patchesForEntry = (LinkedList<Patch>) dmp.patchFromText(patchEntry.getValue());
					Object[] result = dmp.patchApply(patchesForEntry, previous);
					// TODO: Check if all patches could be applied.
					if (((String) result[0]).isEmpty())
						songEntries.remove(patchEntry.getKey());
					else
						songEntries.put(patchEntry.getKey(), (String) result[0]);
				}
				changedSongs.add(new Song(songEntries));
			}
			versionAfterChangingSongs = nextVersion.getId();
		}
		if (!changedSongs.isEmpty()) {
			db.updateSongsByUUID(changedSongs);
			currentVersionId = versionAfterChangingSongs;
			
			// Identify conflicts, remove them from offline changes
			if (offlineChanges != null && !offlineChanges.isEmpty()) {
				LinkedList<Song> conflictChanges = new LinkedList<>();
				for (Song remoteChange : changedSongs) {
					for (Song offlineChange : offlineChanges) {
						if (remoteChange.getUUID().equals(offlineChange.getUUID())) {
							conflictChanges.add(offlineChange);
						}
					}
				}
				offlineChanges.removeAll(conflictChanges);
				conflicts.addAll(conflictChanges);
			}
			
			super.updateSongs(changedSongs);
		}
		publishOfflineChanges();
	}
	
	/**
	 * Call this: To collect and send local changes.
	 * This may take some time and as it compares the whole offline list with the current db.
	 * It will be only done once, and if the db is up to date, to avoid server side rejects.
	 * 
	 * TODO: Currently, online changes are preferred, as it runs after all online changes are applied.
	 * So offline changes may be overridden.
	 */
	private void publishOfflineChanges() {
		if (offlineChanges != null &&
			remoteController.getLatestVersion().get().getId() == currentVersionId
			&& remoteController.getHealthDB().get() == Health.online) {
			changeSongs(offlineChanges);
			offlineChanges = null;
		}
	}
	
	/**
	 * To collect differences with the db and also update the given songs to match with the db.
	 *
	 * @param songs
	 * @param alignWithDB
	 * @return
	 */
	private List<Song> collectChanges(SongsModel songs) {
		List<Song> changes = new LinkedList<>();
		Map<String, Song> dbMap = db.toMap();
		Map<String, Song> songsMap = songs.toMap();
		// If song is has been changed in songs:
		for (Map.Entry<String, Song> songsEntry : songsMap.entrySet()) {
			if (dbMap.containsKey(songsEntry.getKey())) {
				Song dbSong = dbMap.get(songsEntry.getKey());
				if (!songsEntry.getValue().equals(dbSong)) {
					changes.add(songsEntry.getValue());
				}
			} else {
				// New Song in songs:
				changes.add(songsEntry.getValue());
			}
		}
		// If song has been removed in songs, add an empty one to changes:
		for (String key : dbMap.keySet()) {
			if (!songsMap.containsKey(key)) {
				changes.add(new Song(key));
			}
		}
		return changes;
	}
	
	/**
	 * To add, change or delete a songs in the synchronized database.
	 *
	 * @param newSongs
	 */
	private void changeSongs(Iterable<Song> newSongs) {
		final DiffMatchPatch dmp = new DiffMatchPatch();
		
		LinkedList<Song> patchSongs = new LinkedList<>();
		for (Song newSong : newSongs) {
			Song songFromDB = db.getByUUID(newSong.getUUID());
			if (songFromDB == null)
				songFromDB = new Song(newSong.getUUID());
			Map<String, String> songEntries = songFromDB.toMap();
			HashMap<String, String> patchSongMap = new HashMap<>();
			for (Map.Entry<String, String> newSongEntry : newSong.toMap().entrySet()) {
				if (newSongEntry.getKey().equals("uuid"))
					continue;
				String textNew = newSongEntry.getValue();
				String textOld = songEntries.get(newSongEntry.getKey());
				if (textNew == null)
					textNew = "";
				if (textOld == null)
					textOld = "";
				String patchesText = dmp.patchToText(dmp.patchMake(textOld, textNew));
				if (!patchesText.isEmpty())
					patchSongMap.put(newSongEntry.getKey(), patchesText);
			}
			Song patchSong = new Song(newSong.getUUID());
			patchSong.fromMap(patchSongMap);
			if (!patchSong.isEmpty())
				patchSongs.add(patchSong);
		}
		
		if (patchSongs.size() > 0) {
			long newVersionId = currentVersionId + 1;
			String uuid = UUID.randomUUID().toString();
			remoteController.getLatestPatch().set(new SongsModel(patchSongs, false), remoteController.getUsername(), String.valueOf(newVersionId),
				uuid);
			// TODO: wait for reject or version change.
		}
	}
	
	/**
	 * To request missing patches if, there are patches missing.
	 */
	private void requestMissingPatches() {
		MqttObject<PatchVersion> latestVersion = remoteController.getLatestVersion();
		if (latestVersion.get() != null && latestVersion.get().getId() > currentVersionId) {
			for (long i = currentVersionId + 1; i <= latestVersion.get().getId(); i += 1)
				remoteController.getRequestGet().set(i);
		}
	}
	
	/**
	 * Save the synchronized database at the current version.
	 */
	@Override
	public boolean save() {
		super.save();
		
		File dbPropertiesFile = new File(FileAndDirectoryLocations.getDefaultDBPropertiesFileName());
		File dbFile = new File(FileAndDirectoryLocations.getDefaultDBFileName());
		
		try (OutputStream xmlOutputStream = new FileOutputStream(dbFile)) {
			LOG.debug("writing db to file \"{}\" and db properties to file \"{}\"", dbFile.getAbsolutePath(), dbPropertiesFile.getAbsolutePath());
			XMLConverter.fromPersistableToXML(db, xmlOutputStream);
			
			Properties properties = new Properties();
			properties.setProperty(PREF_DB_PREFIX, remoteController.getPrefix());
			properties.setProperty(PREF_DB_SERVER, remoteController.getServer());
			properties.setProperty(PREF_SONGS_VERSION_ID, String.valueOf(currentVersionId));
			properties.storeToXML(new FileOutputStream(dbPropertiesFile), PREF_COMMENT);
			
			LOG.debug("Saved db to with version " + currentVersionId);
			return true;
		} catch (IOException e) {
			LOG.error("could not write db", e);
			return false;
		}
	}
	
	@Override
	public void update(SongsModel songs) {
		changeSongs(collectChanges(songs));
	}
	
	@Override
	public boolean updateSongs(Iterable<Song> changedSongs) {
		changeSongs(changedSongs);
		return true;
	}
	
	@Override
	public boolean removeSong(Song songToDelete) {
		changeSongs(Collections.singletonList(new Song(songToDelete.getUUID())));
		return true;
	}
}
