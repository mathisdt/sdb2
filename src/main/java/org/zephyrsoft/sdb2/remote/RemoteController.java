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
import java.util.function.BiPredicate;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;

/**
 * 
 */
public class RemoteController {
	
	private MQTT mqtt;
	private MqttObject<Song> song;
	private MqttObject<SongPosition> songPosition;
	private MqttObject<Boolean> visible;
	
	public RemoteController(String server, String user, String password) {
		mqtt = new MQTT(server, user, password);
		
		// We use a own Equals function instead of Song.equals.
		song = new MqttObject<>(mqtt, null, RemoteTopic.SONG, RemoteController::messageToSong, null, RemoteTopic.SONG,
			RemoteController::songToMessage, 0, false,
			new BiPredicate<Song, Song>() {
				@Override
				public boolean test(Song t, Song u) {
					if (t == null || u == null) {
						return false;
					}
					return t.getUUID().equals(u.getUUID());
				}
			});
		songPosition = new PubSubObject<>(mqtt, RemoteTopic.SONG_POSITION, SongPosition::fromString);
		visible = new PubSubObject<>(mqtt, RemoteTopic.VISIBLE, Boolean::parseBoolean);
	}
	
	/**
	 * To change the server connection on the fly.
	 * 
	 * @param server
	 * @param user
	 * @param password
	 */
	public void updateServerConfig(String server, String user, String password) {
		if (!mqtt.getServerUri().equals(server) || !mqtt.getUserName().equals(user) || !mqtt.getPassword().equals(password)) {
			mqtt.close();
			mqtt = new MQTT(server, user, password);
			
			song.connectTo(mqtt);
			songPosition.connectTo(mqtt);
			visible.connectTo(mqtt);
		}
	}
	
	public MqttObject<Song> getSong() {
		return song;
	}
	
	public MqttObject<SongPosition> getSongPosition() {
		return songPosition;
	}
	
	public MqttObject<Boolean> getVisible() {
		return visible;
	}
	
	private static String songToMessage(Song song) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SongsModel model = new SongsModel();
		model.addSong(song);
		XMLConverter.fromPersistableToXML(model, baos);
		return baos.toString(StandardCharsets.UTF_8);
	}
	
	private static Song messageToSong(String xml) {
		SongsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		return model.getSongs().iterator().next();
	}
	
	public void close() {
		mqtt.close();
	}
	
}
