/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;

/**
 *
 */
public class RemoteController {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteController.class);
	
	private final MQTT mqtt;
	private final MqttObject<Song> song;
	private final MqttObject<SongPosition> songPosition;
	private final MqttObject<SongsModel> playlist;
	private final MqttObject<PatchVersion> latestVersion;
	private final MqttObject<PatchVersion> requestVersion;
	private final MqttObject<SongsModel> latestPatch;
	private final MqttObject<SongsModel> requestPatch;
	private final MqttObject<Long> requestGet;
	private final MqttObject<String> latestReject;
	private final MqttObject<Health> healthDB;
	private final RemotePresenter remotePresenter;
	private final String prefix;
	private final String namespace;
	private final String server;
	private final String username;
	private final String password;
	private final boolean showTitle;
	
	/**
	 * Creates a RemoteController instance by connecting to a broker and setting up properties.
	 * <p>
	 * You may set a prefix if you want to share a broker with multiple instance groups.
	 * The prefix may be a code, where only selected users have access too. It must be set if you want to set up or use
	 * a global mqtt server for multiple organizations.
	 * A Organization may have different namespaces, to split up presentation instances into.
	 * Users may have only access to some namespaces.
	 * <p>
	 * A namespace contains one shared presentation, and multiple playlists.
	 * <p>
	 * All properties are by default without local notify. They can't be used for in program synchronization.
	 * Furthermore they are retained.
	 *
	 * @param mainWindow
	 *            can be null, if headless
	 */
	public RemoteController(SettingsModel settingsModel, MainController mainController, MainWindow mainWindow) throws MqttException {
		prefix = settingsModel.get(SettingKey.REMOTE_PREFIX, String.class);
		namespace = settingsModel.get(SettingKey.REMOTE_NAMESPACE, String.class);
		server = settingsModel.get(SettingKey.REMOTE_SERVER, String.class);
		username = settingsModel.get(SettingKey.REMOTE_USERNAME, String.class);
		password = settingsModel.get(SettingKey.REMOTE_PASSWORD, String.class);
		showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
		
		mqtt = new MQTT(server, username, password);
		
		mqtt.onConnectionLost(cause -> {
			mainController.setRemoteStatus(RemoteStatus.FAILURE);
			cause.printStackTrace();
		});
		
		song = new MqttObject<>(mqtt, formatTopic(RemoteTopic.SONG),
			RemoteController::parseSong, RemoteController::songToString, RemoteTopic.SONG_QOS, RemoteTopic.SONG_RETAINED,
			null);
		if (mainWindow != null)
			song.onRemoteChange((s, a) -> mainWindow.present(s));
		else
			song.onRemoteChange((s, a) -> mainController.present(new Presentable(s, null)));
		
		songPosition = new MqttObject<>(mqtt, formatTopic(RemoteTopic.SONG_POSITION), SongPosition::parseSongPosition,
			null, RemoteTopic.SONG_POSITION_QOS, RemoteTopic.SONG_POSITION_RETAINED,
			null);
		songPosition.onRemoteChange((p, a) -> {
			try {
				mainController.moveToLine(p.getPart(showTitle), p.getLine());
			} catch (IndexOutOfBoundsException e) {
				LOG.warn("Part or line out of bounds!");
			}
			
		});
		// TODO: add mainwindow caller
		
		if (mainWindow != null) {
			playlist = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PLAYLIST),
				RemoteController::parseSongsModel,
				RemoteController::songsModelToString, RemoteTopic.PLAYLIST_QOS, RemoteTopic.PLAYLIST_RETAINED,
				null);
			playlist.onRemoteChange((p, a) -> mainWindow.getPresentModel().update(p));
			mainWindow.getPresentModel().addSongsModelListener(() -> playlist.set(new SongsModel(mainWindow.getPresentModel())));
			
			latestVersion = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_VERSION),
				PatchVersion::parsePatchVersion,
				null, RemoteTopic.PATCHES_LATEST_VERSION_QOS, RemoteTopic.PATCHES_LATEST_VERSION_RETAINED,
				(a, b) -> false);
			
			latestPatch = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_PATCH),
				RemoteController::parseSongsModel,
				RemoteController::songsModelToString, RemoteTopic.PATCHES_LATEST_PATCH_QOS, RemoteTopic.PATCHES_LATEST_PATCH_RETAINED,
				(a, b) -> false);
			
			latestReject = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_REJECT),
				null,
				null, RemoteTopic.PATCHES_LATEST_REJECT_QOS, RemoteTopic.PATCHES_LATEST_REJECT_RETAINED,
				(a, b) -> false);
			
			requestGet = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_GET),
				String::valueOf, RemoteTopic.PATCHES_REQUEST_GET_QOS, RemoteTopic.PATCHES_REQUEST_GET_RETAINED);
			
			requestVersion = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_VERSION),
				PatchVersion::parsePatchVersion,
				null, RemoteTopic.PATCHES_REQUEST_VERSION_QOS, RemoteTopic.PATCHES_REQUEST_VERSION_RETAINED,
				(a, b) -> false);
			
			requestPatch = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_PATCH),
				RemoteController::parseSongsModel,
				RemoteController::songsModelToString, RemoteTopic.PATCHES_REQUEST_PATCH_QOS, RemoteTopic.PATCHES_REQUEST_PATCH_RETAINED,
				(a, b) -> false);
			
			healthDB = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.HEALTH_DB),
				Health::valueOf, null, RemoteTopic.PATCHES_REQUEST_PATCH_QOS, RemoteTopic.PATCHES_REQUEST_PATCH_RETAINED, null);
		} else {
			this.playlist = null;
			this.requestVersion = null;
			this.requestPatch = null;
			this.requestGet = null;
			this.latestVersion = null;
			this.latestReject = null;
			this.latestPatch = null;
			this.healthDB = null;
		}
		
		remotePresenter = new RemotePresenter(this, showTitle);
	}
	
	public MqttObject<Song> getSong() {
		return song;
	}
	
	public MqttObject<SongsModel> getPlaylist() {
		return playlist;
	}
	
	public MqttObject<SongPosition> getSongPosition() {
		return songPosition;
	}
	
	public RemotePresenter getRemotePresenter(Presentable presentable) {
		remotePresenter.setContent(presentable);
		return remotePresenter;
	}
	
	/**
	 * Converts a song object to string.
	 *
	 * @return song as string
	 */
	private static String songToString(Song song) {
		SongsModel model = new SongsModel();
		if (song != null)
			model.addSong(song);
		return songsModelToString(model);
	}
	
	/**
	 * Parses a song string to a song object.
	 *
	 * @return song as string
	 */
	private static Song parseSong(String xml) {
		SongsModel model = parseSongsModel(xml);
		return model.getSongs().size() == 1 ? model.getSongs().iterator().next() : null;
	}
	
	/**
	 * Converts a SongsModel object to string.
	 *
	 * @return SongsModel as string
	 */
	private static String songsModelToString(SongsModel songsModel) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLConverter.fromPersistableToXML(songsModel, baos, false);
		return baos.toString(StandardCharsets.UTF_8);
	}
	
	/**
	 * Parses a SongsModel string to a SongsModel object.
	 *
	 * @return SongsModel instance
	 */
	static SongsModel parseSongsModel(String xml) {
		return XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}
	
	/**
	 * Closes the mqtt connection.
	 */
	public void close() {
		mqtt.close();
	}
	
	private String formatTopic(String topic) {
		return String.format(topic, prefix.isBlank() ? "" : prefix + "/", namespace);
	}
	
	private String formatClientIDTopic(String topic) {
		return String.format(topic, prefix.isBlank() ? "" : prefix + "/", mqtt.getClientID());
	}
	
	public boolean checkSettingsChanged(SettingsModel settings) {
		String sPrefix = settings.get(SettingKey.REMOTE_PREFIX, String.class);
		String sNamespace = settings.get(SettingKey.REMOTE_NAMESPACE, String.class);
		String sServer = settings.get(SettingKey.REMOTE_SERVER, String.class);
		String sUsername = settings.get(SettingKey.REMOTE_USERNAME, String.class);
		String sPassword = settings.get(SettingKey.REMOTE_PASSWORD, String.class);
		
		return !sPrefix.equals(prefix) || !sNamespace.equals(namespace) || !sServer.equals(server) || !sUsername.equals(sUsername) || !sPassword
			.equals(password);
	}
	
	public MqttObject<PatchVersion> getLatestVersion() {
		return latestVersion;
	}
	
	public MqttObject<PatchVersion> getRequestVersion() {
		return requestVersion;
	}
	
	public MqttObject<SongsModel> getLatestPatch() {
		return latestPatch;
	}
	
	public MqttObject<SongsModel> getRequestPatch() {
		return requestPatch;
	}
	
	public MqttObject<Long> getRequestGet() {
		return requestGet;
	}
	
	public MqttObject<String> getLatestReject() {
		return latestReject;
	}
	
	public MqttObject<Health> getHealthDB() {
		return healthDB;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getServer() {
		return server;
	}
	
}
