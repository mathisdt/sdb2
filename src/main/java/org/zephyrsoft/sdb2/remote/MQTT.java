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

import java.util.Observable;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MQTT extends Observable implements MqttCallback {
	
	private MqttClient client;
	private String serverUri;
	private String clientID;
	private String userName;
	private String password;
	
	public MQTT(String serverUri) {
		this(serverUri, UUID.randomUUID().toString(), null, null);
	}
	
	public MQTT(String serverUri, String userName, String password) {
		this(serverUri, UUID.randomUUID().toString(), userName, password);
	}
	
	public MQTT(String serverUri, String clientID, String userName, String password) {
		this.serverUri = serverUri;
		this.clientID = clientID;
		this.userName = userName;
		this.password = password;
		
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		if (!userName.isEmpty()) {
			mqttConnectOptions.setUserName(userName);
		}
		if (!password.isEmpty()) {
			mqttConnectOptions.setPassword(password.toCharArray());
		}
		try {
			client = new MqttClient(serverUri, clientID);
			client.connectWithResult(mqttConnectOptions);
			client.setCallback(this);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		cause.printStackTrace();
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Got message: " + topic);
		setChanged();
		notifyObservers(new MQTTMessage(topic, message.toString()));
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Nothing to do here
	}
	
	class MQTTMessage {
		private String topic;
		private String message;
		
		public MQTTMessage(String topic, String message) {
			this.topic = topic;
			this.message = message;
		}
		
		public String getTopic() {
			return topic;
		}
		
		public String getMessage() {
			return message;
		}
	}
	
	public void subscribe(String topic, int qos) {
		try {
			System.out.println("Subscribing: " + topic);
			client.subscribe(topic, qos);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void publish(String topic, String payload, int qos, boolean retained) {
		if (client.isConnected()) {
			try {
				client.publish(topic, payload.getBytes(), qos, retained);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		if (client.isConnected()) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				// e.printStackTrace();
			}
		}
		try {
			client.close();
		} catch (MqttException e) {
			// e.printStackTrace();
		}
	}
	
	public String getServerUri() {
		return serverUri;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}
}
