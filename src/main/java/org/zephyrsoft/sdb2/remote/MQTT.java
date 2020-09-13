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

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTT implements MqttCallback {
	
	private static final Logger LOG = LoggerFactory.getLogger(MQTT.class);
	
	private MqttClient client;
	private String clientID;
	private CopyOnWriteArrayList<OnMessageListener> onMessageListeners = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<OnConnectionLostListener> onConnectionLostListeners = new CopyOnWriteArrayList<>();
	
	public MQTT(String serverUri) throws MqttException {
		this(serverUri, UUID.randomUUID().toString(), null, null);
	}
	
	public MQTT(String serverUri, String userName, String password) throws MqttException {
		this(serverUri, UUID.randomUUID().toString(), userName, password);
	}
	
	/**
	 * A simple MQTTClient wrapper which establishes the connection on object creation,
	 * implements the observable pattern to let multiple Observers receive messages and
	 * handles some exceptions.
	 *
	 * It currently only supports String messages.
	 *
	 * @throws MqttException
	 */
	public MQTT(String serverUri, String clientID, String userName, String password) throws MqttException {
		this.clientID = clientID;
		
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		if (!userName.isEmpty()) {
			mqttConnectOptions.setUserName(userName);
		}
		if (!password.isEmpty()) {
			mqttConnectOptions.setPassword(password.toCharArray());
		}
		client = new MqttClient(serverUri, clientID);
		client.connectWithResult(mqttConnectOptions);
		client.setCallback(this);
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		onConnectionLostListeners.forEach(ocll -> ocll.onConnectionLost(cause));
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		LOG.debug("Got message: {}", topic);
		onMessageListeners.forEach(oml -> oml.onMessage(topic, message.toString()));
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Nothing to do here
	}
	
	public void subscribe(String topic, int qos) throws MqttException {
		LOG.debug("Subscribing: {}", topic);
		client.subscribe(topic, qos);
	}
	
	public void publish(String topic, String payload, int qos, boolean retained) {
		if (client.isConnected()) {
			LOG.debug("Publishing message: {}", topic);
			try {
				client.publish(topic, payload.getBytes(), qos, retained);
			} catch (Exception e) {
				// only log the exception
				LOG.warn("could not publish message", e);
			}
		}
	}
	
	public void close() {
		if (client.isConnected()) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				// do nothing
			}
		}
		try {
			client.close();
		} catch (MqttException e) {
			// do nothing
		}
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public void onMessage(OnMessageListener onMessageListener) {
		onMessageListeners.add(onMessageListener);
	}
	
	public interface OnMessageListener {
		public abstract void onMessage(String topic, String message);
	}
	
	public void onConnectionLost(OnConnectionLostListener onConnectionLostListener) {
		onConnectionLostListeners.add(onConnectionLostListener);
	}
	
	public interface OnConnectionLostListener {
		public abstract void onConnectionLost(Throwable cause);
	}
}
