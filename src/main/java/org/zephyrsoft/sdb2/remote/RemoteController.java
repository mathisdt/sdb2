/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.FileAndDirectoryLocations;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.Persistable;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.SongPresentationPosition;
import org.zephyrsoft.sdb2.util.StringTools;

public class RemoteController {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteController.class);

	private final MqttObject<Song> song;
	private final MqttObject<Position> position;
	private final MqttObject<SongsModel> playlist;
	private final MqttObject<Version> latestVersion;
	private final MqttObject<SongsModel> latestChanges;
	private final MqttObject<PatchRequest> requestGet;
	private final MqttObject<Patches> requestPatches;
	private final MqttObject<ChangeReject> latestReject;
	private MqttObject<byte[]> filesRequestSet;
	private MqttObject<FileRequest> filesRequestGet;
	private MqttObject<FileSetResponse> filesRequestSetResponse;
	private MqttObject<byte[]> filesRequestFile;
	private final MqttObject<Health> healthDB;
	private final RemotePresenter remotePresenter;
	private RemotePreferences remotePreferences;
	private FileController fileController;

	/**
	 * Creates a RemoteController instance by connecting to a broker and setting up
	 * properties.
	 * <p>
	 * You may set a prefix if you want to share a broker with multiple instance
	 * groups. The prefix may be a code, where only selected users have access too.
	 * It must be set if you want to set up or use a global mqtt server for multiple
	 * organizations. A Organization may have different rooms, to split up
	 * presentation instances into. Users may have only access to some rooms.
	 * <p>
	 * A room contains one shared presentation, and multiple playlists.
	 * <p>
	 * All properties are by default without local notify. They can't be used for in
	 * program synchronization. Furthermore they are retained.
	 *
	 * @param mainWindow can be null, if headless
	 */
	public RemoteController(RemotePreferences remotePreferences, MainController mainController, MainWindow mainWindow) {
		this.remotePreferences = remotePreferences;

		position = new MqttObject<>(formatTopic(RemoteTopic.POSITION), (s) -> (Position) parseXML(s),
				RemoteController::toXML, RemoteTopic.POSITION_QOS, RemoteTopic.POSITION_RETAINED, null);
		position.onRemoteChange((p, a) -> updateSongOrPosition(mainController, mainWindow));

		song = new MqttObject<>(formatTopic(RemoteTopic.SONG), (s) -> (Song) parseXML(s), RemoteController::toXML,
				RemoteTopic.SONG_QOS, RemoteTopic.SONG_RETAINED, null);
		song.onRemoteChange((s, a) -> updateSongOrPosition(mainController, mainWindow));

		filesRequestGet = new MqttObject<>(formatClientIDTopic(RemoteTopic.FILES_REQUEST_GET),
				RemoteController::toXML, RemoteTopic.FILES_REQUEST_GET_QOS, RemoteTopic.FILES_REQUEST_GET_RETAINED);

		filesRequestFile = new MqttObject<>(formatClientIDTopic(RemoteTopic.FILES_REQUEST_FILE), null, null,
				RemoteTopic.FILES_REQUEST_FILE_QOS, RemoteTopic.FILES_REQUEST_FILE_RETAINED, (a, b) -> false);

		if (mainWindow != null) {
			playlist = new MqttObject<>(formatTopic(RemoteTopic.PLAYLIST), (s) -> (SongsModel) parseXML(s),
					RemoteController::toXML, RemoteTopic.PLAYLIST_QOS, RemoteTopic.PLAYLIST_RETAINED, null);
			playlist.onRemoteChange((p, a) -> fileController.downloadFiles(p, () -> mainWindow.updatePlaylist(p)));
			mainWindow.getPresentModel().addSongsModelListener(() -> fileController.uploadFiles(mainWindow.getPresentModel(), (s) -> playlist.set(new SongsModel(s))));

			latestVersion = new MqttObject<>(formatTopic(RemoteTopic.PATCHES_LATEST_VERSION),
					(s) -> (Version) parseXML(s), RemoteController::toXML, RemoteTopic.PATCHES_LATEST_VERSION_QOS,
					RemoteTopic.PATCHES_LATEST_VERSION_RETAINED, (a, b) -> false);

			latestChanges = new MqttObject<>(formatTopic(RemoteTopic.PATCHES_LATEST_CHANGES),
					(s) -> (SongsModel) parseXML(s), RemoteController::toXML, RemoteTopic.PATCHES_LATEST_CHANGES_QOS,
					RemoteTopic.PATCHES_LATEST_CHANEGS_RETAINED, (a, b) -> false);

			latestReject = new MqttObject<>(formatTopic(RemoteTopic.PATCHES_LATEST_REJECT),
					(s) -> (ChangeReject) parseXML(s), null, RemoteTopic.PATCHES_LATEST_REJECT_QOS,
					RemoteTopic.PATCHES_LATEST_REJECT_RETAINED, (a, b) -> false);

			requestGet = new MqttObject<>(formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_GET), RemoteController::toXML,
					RemoteTopic.PATCHES_REQUEST_GET_QOS, RemoteTopic.PATCHES_REQUEST_GET_RETAINED);

			requestPatches = new MqttObject<>(formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_PATCHES),
					(s) -> (Patches) parseXML(s), RemoteController::toXML, RemoteTopic.PATCHES_REQUEST_PATCHES_QOS,
					RemoteTopic.PATCHES_REQUEST_PATCHES_RETAINED, (a, b) -> false);

			filesRequestSet = new MqttObject<>(formatClientIDTopic(RemoteTopic.FILES_REQUEST_SET), (b) -> b, 
					RemoteTopic.FILES_REQUEST_SET_QOS, RemoteTopic.FILES_REQUEST_SET_RETAINED);

			filesRequestSetResponse = new MqttObject<>(formatClientIDTopic(RemoteTopic.FILES_REQUEST_SET_RESPONSE),
					(s) -> (FileSetResponse) parseXML(s), null, RemoteTopic.FILES_REQUEST_SET_RESPONSE_QOS,
					RemoteTopic.FILES_REQUEST_SET_RESPONSE_RETAINED, (a, b) -> false);

			healthDB = new MqttObject<>(formatPrefixTopic(RemoteTopic.HEALTH_DB), Health::valueOfBytes, null,
					RemoteTopic.HEALTH_DB_QOS, RemoteTopic.HEALTH_DB_RETAINED, null);
		} else {
			this.playlist = null;
			this.requestPatches = null;
			this.requestGet = null;
			this.latestVersion = null;
			this.latestReject = null;
			this.latestChanges = null;
			this.healthDB = null;
			this.filesRequestSet = null;
			this.filesRequestSetResponse = null;
		}
		
		fileController = new FileController(this);
		remotePresenter = new RemotePresenter(this);
	}

	public void connectTo(MQTT mqtt) throws MqttException {
		song.connectTo(mqtt);
		position.connectTo(mqtt);
		playlist.connectTo(mqtt);
		latestVersion.connectTo(mqtt);
		latestChanges.connectTo(mqtt);
		requestGet.connectTo(mqtt);
		requestPatches.connectTo(mqtt);
		latestReject.connectTo(mqtt);
		healthDB.connectTo(mqtt);
		filesRequestFile.connectTo(mqtt);
		filesRequestGet.connectTo(mqtt);
		filesRequestSet.connectTo(mqtt);
		filesRequestSetResponse.connectTo(mqtt);
	}

	private void updateSongOrPosition(MainController mainController, MainWindow mainWindow) {
		if (position == null || song == null)
			return;
		Position p = position.get();
		if (p == null) {
			// presentSong(mainController, mainWindow, null);
			return;
		}
		Song s = song.get();
		if (s == null) {
			return;
		}

		if (StringTools.equals(s.getUUID(), p.getUUID())) {
			if (p.isVisible()) {
				p = StringTools.isEmpty(s.getImage()) ? p : null;
				presentSong(mainController, mainWindow, s, p);
			} else {
				presentSong(mainController, mainWindow, null, null);
			}
		}
	}

	private void presentSong(MainController mainController, MainWindow mainWindow, Song s, Position p) {
		SongPresentationPosition spp = p != null ? new SongPresentationPosition(p.getPart(), p.getLine()) : null;
		if (mainWindow != null) {
			mainWindow.present(s, spp);
		} else {
			mainController.present(new Presentable(s, null), spp);
		}
	}

	public MqttObject<Song> getSong() {
		return song;
	}

	public MqttObject<SongsModel> getPlaylist() {
		return playlist;
	}

	public MqttObject<Position> getPosition() {
		return position;
	}

	public RemotePresenter getRemotePresenter(Presentable presentable) {
		remotePresenter.setContent(presentable, null);
		return remotePresenter;
	}

	static Persistable parseXML(byte[] xml) {
		if (xml.length == 0)
			return null;
		return XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(xml));
	}

	static byte[] toXML(Persistable persistable) {
		if (persistable == null)
			return new byte[0];
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XMLConverter.fromPersistableToXML(persistable, baos, false, true);
			return baos.toByteArray();
		} catch (IllegalStateException e) {
			LOG.warn("Could convert persistable to payload.", e);
		}
		return null;
	}

	private String formatTopic(String topic) {
		return String.format(topic,
				getRemotePreferences().getPrefix().isBlank() ? "" : getRemotePreferences().getPrefix() + "/",
				getRemotePreferences().getRoom());
	}

	private String formatClientIDTopic(String topic) {
		return String.format(topic,
				getRemotePreferences().getPrefix().isBlank() ? "" : getRemotePreferences().getPrefix() + "/",
				remotePreferences.getClientID());
	}

	private String formatPrefixTopic(String topic) {
		return String.format(topic,
				getRemotePreferences().getPrefix().isEmpty() ? "" : getRemotePreferences().getPrefix() + "/");
	}

	public MqttObject<Version> getLatestVersion() {
		return latestVersion;
	}

	public MqttObject<SongsModel> getLatestChanges() {
		return latestChanges;
	}

	public MqttObject<Patches> getRequestPatches() {
		return requestPatches;
	}

	public MqttObject<PatchRequest> getRequestGet() {
		return requestGet;
	}

	public MqttObject<ChangeReject> getLatestReject() {
		return latestReject;
	}

	public MqttObject<Health> getHealthDB() {
		return healthDB;
	}

	public RemotePreferences getRemotePreferences() {
		return remotePreferences;
	}

	public MqttObject<byte[]> getFilesRequestSet() {
		return filesRequestSet;
	}

	public MqttObject<FileRequest> getFilesRequestGet() {
		return filesRequestGet;
	}

	public MqttObject<byte[]> getFilesRequestFile() {
		return filesRequestFile;
	}

	public MqttObject<FileSetResponse> getFilesRequestSetResponse() {
		return filesRequestSetResponse;
	}

}
