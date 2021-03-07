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

public class RemoteController {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteController.class);
	
	private final MQTT mqtt;
	private final MqttObject<Song> song;
	private final MqttObject<SongPosition> songPosition;
	private final MqttObject<SongsModel> playlist;
	private final RemotePresenter remotePresenter;
	private final String prefix;
	private final String namespace;
	private final String server;
	private final String username;
	private final String password;
	private final boolean showTitle;
	
	/**
	 * Creates a RemoteController instance by connecting to a broker and setting up properties.
	 *
	 * You may set a prefix if you want to share a broker with multiple instance groups.
	 * The prefix may be a code, where only selected users have access too. It must be set if you want to set up or use
	 * a global mqtt server for multiple organizations.
	 * A Organization may have different namespaces, to split up presentation instances into.
	 * Users may have only access to some namespaces.
	 *
	 * A namespace contains one shared presentation, and multiple playlists.
	 *
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
		showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class).booleanValue();
		
		mqtt = new MQTT(server, username, password);
		
		mqtt.onConnectionLost(cause -> {
			mainController.setRemoteStatus(RemoteStatus.FAILURE);
			cause.printStackTrace();
		});
		
		song = new PubSubObject<>(mqtt, formatTopic(RemoteTopic.SONG), RemoteController::parseSong, RemoteController::songToString, true);
		if (mainWindow != null)
			song.onRemoteChange(s -> mainWindow.present(s));
		else
			song.onRemoteChange(s -> mainController.present(new Presentable(s, null)));
		
		songPosition = new PubSubObject<>(mqtt, formatTopic(RemoteTopic.SONG_POSITION), SongPosition::parseSongPosition, true);
		songPosition.onRemoteChange(p -> {
			try {
				mainController.moveToLine(p.getPart(showTitle), p.getLine());
			} catch (IndexOutOfBoundsException e) {
				LOG.warn("Part or line out of bounds!");
			}
			
		});
		// TODO: add mainwindow caller
		
		if (mainWindow != null) {
			playlist = new PubSubObject<>(mqtt, formatTopic(RemoteTopic.PLAYLIST), RemoteController::parseSongsModel,
				RemoteController::songsModelToString,
				true);
			playlist.onRemoteChange(p -> mainWindow.getPresentModel().update(p));
			mainWindow.getPresentModel().addSongsModelListener(() -> playlist.set(new SongsModel(mainWindow.getPresentModel())));
		} else {
			this.playlist = null;
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
		XMLConverter.fromPersistableToXML(songsModel, baos);
		return baos.toString(StandardCharsets.UTF_8);
	}
	
	/**
	 * Parses a SongsModel string to a SongsModel object.
	 *
	 * @return SongsModel instance
	 */
	private static SongsModel parseSongsModel(String xml) {
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
	
	public boolean checkSettingsChanged(SettingsModel settings) {
		String sPrefix = settings.get(SettingKey.REMOTE_PREFIX, String.class);
		String sNamespace = settings.get(SettingKey.REMOTE_NAMESPACE, String.class);
		String sServer = settings.get(SettingKey.REMOTE_SERVER, String.class);
		String sUsername = settings.get(SettingKey.REMOTE_USERNAME, String.class);
		String sPassword = settings.get(SettingKey.REMOTE_PASSWORD, String.class);
		
		return !sPrefix.equals(prefix) || !sNamespace.equals(namespace) || !sServer.equals(server) || !sUsername.equals(sUsername) || !sPassword
			.equals(password);
	}
	
}
