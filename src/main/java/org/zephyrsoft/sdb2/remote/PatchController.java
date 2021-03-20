package org.zephyrsoft.sdb2.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.FileAndDirectoryLocations;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.util.SongsModelListener;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;

public class PatchController implements SongsModelListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteController.class);
	
	private static final String PREF_COMMENT = "RemoteController DB Properties";
	private static final String PREF_DB_PREFIX = "PREF_DB_PREFIX";
	private static final String PREF_SONGS_VERSION_ID = "PREF_SONGS_VERSION_ID";
	private final HashMap<String, String> patchMap = new HashMap<>();
	private final HashMap<Long, PatchVersion> patchVersions = new HashMap<>();
	private final HashSet<String> rejects = new HashSet<>();
	private long currentVersionId;
	private RemoteController remoteController;
	private boolean initialRequestMissingPatches;
	private Properties properties = new Properties();
	private MainController mainController;
	private SongsModel db;
	
	public PatchController(RemoteController remoteController, MainController mainController) {
		this.mainController = mainController;
		this.remoteController = remoteController;
		
		initDB();
		
		remoteController.getLatestPatch().onChange(this::addPatch);
		remoteController.getRequestPatch().onChange(this::addPatch);
		remoteController.getLatestVersion().onChange(this::addPatchVersion);
		remoteController.getRequestVersion().onChange(this::addPatchVersion);
		remoteController.getLatestReject().onChange(this::rejectPatch);
		
		addPatchVersion(remoteController.getLatestVersion().get());
		
		// Listen for songs-model changes/get initial changes:
		mainController.getSongs().addSongsModelListener(this);
		songsModelChanged();
	}
	
	private String getDBPrefix() {
		return properties.getProperty(PREF_DB_PREFIX, null);
	}
	
	/**
	 * Resets the database if prefix has changed, reads the current database version resets the
	 * requestMissingPatch boolean.
	 */
	private void initDB() {
		loadDB();
		loadDBProperties();
		
		if (!remoteController.getPrefix().equals(properties.getProperty(PREF_DB_PREFIX, ""))) {
			patchVersions.clear();
			rejects.clear();
			patchMap.clear();
			resetDB();
			properties.setProperty(PREF_DB_PREFIX, remoteController.getPrefix());
			saveDBProperties();
		}
		currentVersionId = Long.parseLong(properties.getProperty(PREF_SONGS_VERSION_ID, "0"));
		LOG.debug("db at prefix \"" + remoteController.getPrefix() + "\" and version: " + currentVersionId);
		
		initialRequestMissingPatches = false;
	}
	
	/**
	 * To reject patches.
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
		if (patchVersion == null || patchVersion.getId() == currentVersionId)
			return;
		
		if (patchVersion.getId() > currentVersionId) {
			patchVersions.put(patchVersion.getId(), patchVersion);
			applyPatches();
			if (!initialRequestMissingPatches) {
				requestMissingPatches();
				initialRequestMissingPatches = true;
			}
		} else if (patchVersion.getId() < currentVersionId) {
			// Currently the only way is to reset the whole database and load all patches again.
			resetDB();
			addPatchVersion(patchVersion);
			requestMissingPatches();
		}
	}
	
	/**
	 * This will reset the local database to version 0.
	 * You have to request missing patches again to reset to the latest version.
	 */
	private void resetDB() {
		db.update(new SongsModel());
		mainController.getSongs().removeSongsModelListener(this);
		mainController.getSongs().update(new SongsModel());
		mainController.getSongs().addSongsModelListener(this);
		// TODO: allow saving empty songmodels:
		if (saveDB()) {
			currentVersionId = 0;
			properties.setProperty(PREF_SONGS_VERSION_ID, String.valueOf(currentVersionId));
			saveDBProperties();
			LOG.debug("Resetted db to version " + currentVersionId);
		} else {
			LOG.error("Could not save songs");
		}
	}
	
	/**
	 * Add a new patch to the patchMap and apply if the version exists and if it's not rejected.
	 *
	 * @param patch
	 */
	private void addPatch(String patch) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(patch.getBytes());
			byte[] digest = md.digest();
			String hash = DatatypeConverter
				.printHexBinary(digest); // .toUpperCase()
			
			// String hash = SecurityUtils.md5(patch);
			if (rejects.contains(hash))
				rejects.remove(hash);
			else
				patchMap.put(hash, patch);
			applyPatches();
		} catch (NoSuchAlgorithmException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	/**
	 * This will apply all recently received patches.
	 */
	private void applyPatches() {
		final DiffMatchPatch dmp = new DiffMatchPatch();
		while (patchVersions.containsKey(currentVersionId + 1) && patchMap.containsKey(patchVersions.get(currentVersionId + 1).getHash())) {
			PatchVersion nextVersion = patchVersions.get(currentVersionId + 1);
			String nextPatch = patchMap.get(nextVersion.getHash());
			Collection<Song> patch = RemoteController.parseSongsModel(nextPatch).getSongs();
			for (Song patchSong : patch) {
				Song song = db.getByUUID(patchSong.getUUID());
				if (song == null)
					song = new Song(patchSong.getUUID());
				Map<String, String> songFromDBMap = song.toMap();
				for (Map.Entry<String, String> patchEntry : patchSong.toMap().entrySet()) {
					if (patchEntry.getKey().equals("uuid") || patchEntry.getValue() == null || patchEntry.getValue().isEmpty())
						continue;
					
					String previous = songFromDBMap.containsKey(patchEntry.getKey()) && songFromDBMap.get(patchEntry.getKey()) != null ? songFromDBMap
						.get(patchEntry.getKey()) : "";
					LinkedList<Patch> patchesForEntry = (LinkedList<Patch>) dmp.patchFromText(patchEntry.getValue());
					Object[] result = dmp.patchApply(patchesForEntry, previous);
					// TODO: Check if all patches could be applied.
					songFromDBMap.put(patchEntry.getKey(), (String) result[0]);
				}
				song.fromMap(songFromDBMap);
				db.updateSongByUUID(song);
				mainController.getSongs().removeSongsModelListener(this);
				mainController.getSongs().updateSongByUUID(song);
				mainController.getSongs().addSongsModelListener(this);
			}
			
			if (saveDB()) {
				currentVersionId = nextVersion.getId();
				properties.setProperty(PREF_SONGS_VERSION_ID, String.valueOf(currentVersionId));
				saveDBProperties();
				LOG.debug("Changed db to version " + currentVersionId);
			} else {
				LOG.error("Could not save songs!");
			}
		}
	}
	
	/**
	 * To add, change or delete a songs in the synchronized database.
	 *
	 * @param newSongs
	 */
	public void changeSongs(SongsModel newSongs) {
		final DiffMatchPatch dmp = new DiffMatchPatch();
		
		LinkedList<Song> patchSongs = new LinkedList<>();
		for (Song newSong : newSongs) {
			Song songFromDB = db.getByUUID(newSong.getUUID());
			if (songFromDB == null)
				songFromDB = new Song(newSong.getUUID());
			Map<String, String> songFromDBMap = songFromDB.toMap();
			HashMap<String, String> patchSongMap = new HashMap<>();
			for (Map.Entry<String, String> newSongEntry : newSong.toMap().entrySet()) {
				if (newSongEntry.getKey().equals("uuid"))
					continue;
				String textNew = newSongEntry.getValue();
				String textOld = songFromDBMap.get(newSongEntry.getKey());
				if (textNew == null)
					textNew = "";
				if (textOld == null)
					textOld = "";
				String patchesText = dmp.patchToText(dmp.patchMake(textOld, textNew));
				patchSongMap.put(newSongEntry.getKey(), patchesText);
			}
			Song patchSong = new Song(newSong.getUUID());
			patchSong.fromMap(patchSongMap);
			if (!patchSong.isEmpty())
				patchSongs.add(patchSong);
		}
		
		if (patchSongs.size() > 0) {
			long newVersionId = currentVersionId + 1;
			remoteController.publishPatches(new SongsModel(patchSongs, false), newVersionId);
			// TODO: wait for reject or version change.
		}
	}
	
	/**
	 * To request missing patches if, there are patches missing.
	 */
	private void requestMissingPatches() {
		if (remoteController == null)
			return;
		MqttObject<PatchVersion> latestVersion = remoteController.getLatestVersion();
		if (latestVersion.get() != null && latestVersion.get().getId() > currentVersionId) {
			for (long i = currentVersionId + 1; i <= latestVersion.get().getId(); i += 1)
				remoteController.getRequestGet().set(i);
		}
	}
	
	private void loadDBProperties() {
		File f = dbPropertiesFile();
		if (!f.exists() || f.isDirectory())
			return;
		
		try {
			properties.loadFromXML(new FileInputStream(f));
		} catch (InvalidPropertiesFormatException e) {
			// TODO
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO
			e.printStackTrace();
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	private File dbPropertiesFile() {
		return new File(FileAndDirectoryLocations.getSongsFileName("db.properties.xml"));
	}
	
	private void saveDBProperties() {
		try {
			properties.storeToXML(new FileOutputStream(dbPropertiesFile()), PREF_COMMENT);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	private File dbFile() {
		return new File(FileAndDirectoryLocations.getSongsFileName("db.xml"));
	}
	
	private boolean saveDB() {
		File file = dbFile();
		try (OutputStream xmlOutputStream = new FileOutputStream(file)) {
			LOG.debug("writing db to file \"{}\"", file.getAbsolutePath());
			XMLConverter.fromPersistableToXML(db, xmlOutputStream);
			return true;
		} catch (IOException e) {
			LOG.error("could not write songs to backup file \"" + file.getAbsolutePath() + "\"", e);
			return false;
		}
	}
	
	private boolean loadDB() {
		File file = dbFile();
		if (!file.exists()) {
			LOG.debug("not reading songs from {} (file does not exist) but using empty model", file.getAbsolutePath());
			db = new SongsModel();
			return true;
		}
		LOG.debug("loading db from file {}", file.getAbsolutePath());
		try {
			db = XMLConverter.fromXMLToPersistable(new FileInputStream(file));
			if (db != null) {
				return true;
			} else {
				LOG.error("could not load songs from {}", file.getAbsolutePath());
				ErrorDialog.openDialogBlocking(null, "Could not load songs from file:\n" + file.getAbsolutePath()
					+ "\n\nThis is a fatal error, exiting.\nSee log file for more details.");
				mainController.shutdown(-1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mainController.shutdown(-1);
		}
		return false;
	}
	
	/**
	 * 
	 */
	public void close() {
		mainController.getSongs().removeSongsModelListener(this);
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.util.SongsModelListener#songsModelChanged()
	 */
	@Override
	public void songsModelChanged() {
		// TODO: Add changed Songs to songsModelChanged.
		changeSongs(mainController.getSongs());
	}
}
