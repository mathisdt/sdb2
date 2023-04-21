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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.IOController;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.StatisticsController;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.SongPresentationPosition;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;

/**
 * Test the interaction with a MQTT broker.
 */
public class RemoteControllerTest {
	private static final Logger LOG = LoggerFactory.getLogger(RemoteControllerTest.class);
	
	private Server mqttServer;
	private MqttClient client;
	
	@Mock
	private IOController ioController;
	@Mock
	private StatisticsController statisticsController;
	@Mock
	private MainController mainController;
	@Mock
	private MainWindow mainWindow;
	
	private SDB2RemotePreferences remotePreferences;
	private RemoteController remoteController;
	
	private static int getFreePort() {
		while (true) {
			try (ServerSocket serverSocket = new ServerSocket(0)) {
				if (serverSocket.getLocalPort() > 1024) {
					LOG.debug("port {} is free", serverSocket.getLocalPort());
					return serverSocket.getLocalPort();
				}
			} catch (IOException e) {
				LOG.warn("could not check for free local port");
			}
		}
	}
	
	private static String temporaryDirectory() {
		try {
			return Files.createTempDirectory("moquette_persistent_store").toFile().getAbsolutePath() + File.separator + "data";
		} catch (IOException e) {
			throw new RuntimeException("could not create temporary file", e);
		}
	}
	
	@Before
	public void setup() throws Exception {
		MockitoAnnotations.openMocks(this);
		when(mainWindow.getPresentModel()).thenAnswer(invocation -> new SongsModel());
		
		mqttServer = new Server();
		Properties configProps = new Properties();
		configProps.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(getFreePort()));
		configProps.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, temporaryDirectory());
		configProps.put(BrokerConstants.ENABLE_TELEMETRY_NAME, "false");
		mqttServer.startServer(configProps);
		
		// .integrationtest as client ID so the temp dir resulting from this is ignored by .gitignore
		client = new MqttClient("tcp://localhost:" + mqttServer.getPort(), ".integrationtest");
		client.connect();
		
		SettingsModel settingsModel = new SettingsModel();
		settingsModel.put(SettingKey.REMOTE_ENABLED, Boolean.TRUE);
		settingsModel.put(SettingKey.REMOTE_PREFIX, "");
		settingsModel.put(SettingKey.REMOTE_NAMESPACE, "default");
		settingsModel.put(SettingKey.REMOTE_SERVER, "tcp://localhost:" + mqttServer.getPort());
		settingsModel.put(SettingKey.REMOTE_USERNAME, "anything");
		settingsModel.put(SettingKey.REMOTE_PASSWORD, "anything");
		remotePreferences = new SDB2RemotePreferences(settingsModel);
		
		remoteController = new RemoteController(remotePreferences, mainController, mainWindow);
	}
	
	@After
	public void teardown() throws MqttException {
		client.disconnect();
		mqttServer.stopServer();
	}
	
	private void send(String topic, String msg, boolean retained) {
		try {
			MqttMessage message = new MqttMessage(msg.getBytes(StandardCharsets.UTF_8));
			message.setRetained(retained);
			client.publish(topic, message);
		} catch (Exception e) {
			throw new RuntimeException("could not send message", e);
		}
	}
	
	@Test
	public void test() throws Exception {
		String testSongUUID = "c874b2ae-8971-4be3-8c16-c8a6e4329b26";
		String testSong = "<song><title>Ein m&#228;chtiger Test</title><uuid>" + testSongUUID
			+ "</uuid><lyrics>Test Lyrics Line 1\nTest Lyrics Line 2\n\nTest Lyrics Line 3\nTest Lyrics Line 4</lyrics></song>";
		
		send("rooms/v1/default/song",
			testSong,
			true);
		send("rooms/v1/default/position",
			"<position><uuid>" + testSongUUID + "</uuid><part>1</part><line>1</line></position>",
			true);
		send("rooms/v1/default/playlist",
			"<songs>" + testSong + "</songs>",
			true);
		
		MQTT mqtt = new MQTT(remotePreferences.getServer(), remotePreferences.getClientID(), remotePreferences.getUsername(), remotePreferences
			.getPassword(), true);
		remoteController.connectTo(mqtt);
		
		Thread.sleep(1000);
		
		verify(mainWindow).present(any(Song.class), eq(new SongPresentationPosition(1, 1)));
	}
}
