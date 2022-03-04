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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.Persistable;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;

public class RemoteController {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteController.class);
	
	private final MQTT mqtt;
	private final MqttObject<Song> song;
	private final MqttObject<Position> position;
	private final MqttObject<SongsModel> playlist;
	private final MqttObject<Version> latestVersion;
	private final MqttObject<SongsModel> latestChanges;
	private final MqttObject<PatchRequest> requestGet;
	private final MqttObject<Patches> requestPatches;
	private final MqttObject<ChangeReject> latestReject;
	private final MqttObject<Health> healthDB;
	private final RemotePresenter remotePresenter;
	private final String prefix;
	private final String room;
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
	 * A Organization may have different rooms, to split up presentation instances into.
	 * Users may have only access to some rooms.
	 * <p>
	 * A room contains one shared presentation, and multiple playlists.
	 * <p>
	 * All properties are by default without local notify. They can't be used for in program synchronization.
	 * Furthermore they are retained.
	 *
	 * @param mainWindow
	 *            can be null, if headless
	 */
	public RemoteController(SettingsModel settingsModel, MainController mainController, MainWindow mainWindow) throws MqttException {
		prefix = settingsModel.get(SettingKey.REMOTE_PREFIX, String.class);
		room = settingsModel.get(SettingKey.REMOTE_NAMESPACE, String.class);
		server = settingsModel.get(SettingKey.REMOTE_SERVER, String.class);
		username = settingsModel.get(SettingKey.REMOTE_USERNAME, String.class);
		password = settingsModel.get(SettingKey.REMOTE_PASSWORD, String.class);
		showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
		
		mqtt = new MQTT(server, username, password, true);
		
		mqtt.onConnectionLost(cause -> {
			mainController.setRemoteStatus(RemoteStatus.FAILURE);
			cause.printStackTrace();
		});
		
		position = new MqttObject<>(mqtt, formatTopic(RemoteTopic.POSITION), (s) -> (Position) parseXML(s),
			RemoteController::toXML, RemoteTopic.POSITION_QOS, RemoteTopic.POSITION_RETAINED,
			null);
		position.onRemoteChange((p, a) -> handleSongPositionChange(mainController, p));
		
		song = new MqttObject<>(mqtt, formatTopic(RemoteTopic.SONG),
			(s) -> (Song) parseXML(s), RemoteController::toXML, RemoteTopic.SONG_QOS, RemoteTopic.SONG_RETAINED,
			null);
		song.onRemoteChange((s, a) -> handleSongChange(mainController, mainWindow, s));
		
		if (mainWindow != null) {
			playlist = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PLAYLIST),
				(s) -> (SongsModel) parseXML(s),
				RemoteController::toXML, RemoteTopic.PLAYLIST_QOS, RemoteTopic.PLAYLIST_RETAINED,
				null);
			playlist.onRemoteChange((p, a) -> mainWindow.updatePlaylist(p));
			mainWindow.getPresentModel().addSongsModelListener(() -> playlist.set(new SongsModel(mainWindow.getPresentModel())));
			
			latestVersion = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_VERSION),
				(s) -> (Version) parseXML(s),
				RemoteController::toXML, RemoteTopic.PATCHES_LATEST_VERSION_QOS, RemoteTopic.PATCHES_LATEST_VERSION_RETAINED,
				(a, b) -> false);
			
			latestChanges = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_CHANGES),
				(s) -> (SongsModel) parseXML(s),
				RemoteController::toXML, RemoteTopic.PATCHES_LATEST_CHANGES_QOS, RemoteTopic.PATCHES_LATEST_CHANEGS_RETAINED,
				(a, b) -> false);
			
			latestReject = new MqttObject<>(mqtt, formatTopic(RemoteTopic.PATCHES_LATEST_REJECT),
				(s) -> (ChangeReject) parseXML(s),
				null, RemoteTopic.PATCHES_LATEST_REJECT_QOS, RemoteTopic.PATCHES_LATEST_REJECT_RETAINED,
				(a, b) -> false);
			
			requestGet = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_GET),
				RemoteController::toXML, RemoteTopic.PATCHES_REQUEST_GET_QOS, RemoteTopic.PATCHES_REQUEST_GET_RETAINED);
			
			requestPatches = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.PATCHES_REQUEST_PATCHES),
				(s) -> (Patches) parseXML(s),
				RemoteController::toXML, RemoteTopic.PATCHES_REQUEST_PATCHES_QOS, RemoteTopic.PATCHES_REQUEST_PATCHES_RETAINED,
				(a, b) -> false);
			
			healthDB = new MqttObject<>(mqtt, formatClientIDTopic(RemoteTopic.HEALTH_DB),
				Health::valueOfBytes, null, RemoteTopic.HEALTH_DB_QOS, RemoteTopic.HEALTH_DB_RETAINED, null);
		} else {
			this.playlist = null;
			this.requestPatches = null;
			this.requestGet = null;
			this.latestVersion = null;
			this.latestReject = null;
			this.latestChanges = null;
			this.healthDB = null;
		}
		
		remotePresenter = new RemotePresenter(this, showTitle);
	}
	
	private void handleSongChange(MainController mainController, MainWindow mainWindow, Song newSong) {
		if (mainWindow != null)
			mainWindow.present(newSong);
		else
			mainController.present(new Presentable(newSong, null));
		// if(song != null)
		// handleSongPositionChange(mainController, mainWindow, position.get());
	}
	
	private void handleSongPositionChange(MainController mainController, Position p) {
		if (p == null) {
			// handleSongChange(mainController, mainWindow, null);
			return;
		} else if (song != null && song.get() != null && song.get().getUUID() != null && song.get().getUUID().equals(p.getUUID())) {
			try {
				mainController.moveToLine(p.getPart(showTitle), p.getLine());
			} catch (IndexOutOfBoundsException e) {
				LOG.warn("Part or line out of bounds!");
			}
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
		remotePresenter.setContent(presentable);
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLConverter.fromPersistableToXML(persistable, baos, false, true);
		return baos.toByteArray();
	}
	
	/**
	 * Closes the mqtt connection.
	 */
	public void close() {
		mqtt.close();
	}
	
	private String formatTopic(String topic) {
		return String.format(topic, prefix.isBlank() ? "" : prefix + "/", room);
	}
	
	private String formatClientIDTopic(String topic) {
		return String.format(topic, prefix.isBlank() ? "" : prefix + "/", mqtt.getClientID());
	}
	
	public boolean checkSettingsChanged(SettingsModel settings) {
		String sPrefix = settings.get(SettingKey.REMOTE_PREFIX, String.class);
		String sRoom = settings.get(SettingKey.REMOTE_NAMESPACE, String.class);
		String sServer = settings.get(SettingKey.REMOTE_SERVER, String.class);
		String sUsername = settings.get(SettingKey.REMOTE_USERNAME, String.class);
		String sPassword = settings.get(SettingKey.REMOTE_PASSWORD, String.class);
		
		return !sPrefix.equals(prefix) || !sRoom.equals(room) || !sServer.equals(server) || !sUsername.equals(sUsername) || !sPassword
			.equals(password);
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
