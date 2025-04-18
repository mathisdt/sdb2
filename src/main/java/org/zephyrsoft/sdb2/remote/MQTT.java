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

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTT implements MqttCallback {
	
	private static final Logger LOG = LoggerFactory.getLogger(MQTT.class);
	
	private final MqttClient client;
	private final CopyOnWriteArrayList<OnMessageListener> onMessageListeners = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<OnConnectionLostListener> onConnectionLostListeners = new CopyOnWriteArrayList<>();
	
	public MQTT(String serverUri, String userName, String password, boolean cleanSession) throws MqttException {
		this(serverUri, UUID.randomUUID().toString(), userName, password, cleanSession);
	}
	
	/**
	 * A simple MQTTClient wrapper which establishes the connection on object creation,
	 * implements the observable pattern to let multiple Observers receive messages and
	 * handles some exceptions.
	 * <p>
	 * It currently only supports String messages.
	 */
	public MQTT(String serverUri, String clientID, String userName, String password, boolean cleanSession) throws MqttException {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		if (!userName.isEmpty()) {
			mqttConnectOptions.setUserName(userName);
		}
		if (!password.isEmpty()) {
			mqttConnectOptions.setPassword(password.toCharArray());
		}
		mqttConnectOptions.setCleanSession(cleanSession);
		
		// Connect without persistence if cleanSession is True:
		MqttClientPersistence persistence = cleanSession ? new MqttDefaultFilePersistence() : null;
		client = new MqttClient(serverUri, clientID, persistence);
		client.connectWithResult(mqttConnectOptions);
		client.setCallback(this);
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		onConnectionLostListeners.forEach(ocll -> ocll.onConnectionLost(cause));
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) {
		LOG.debug("Got message: {}", topic);
		onMessageListeners.forEach(oml -> oml.onMessage(topic, message.getPayload()));
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Nothing to do here
	}
	
	public void subscribe(String topic, int qos) throws MqttException {
		LOG.debug("Subscribing: {}", topic);
		client.subscribe(topic, qos);
	}
	
	public void publish(String topic, byte[] payload, int qos, boolean retained) {
		if (payload == null) {
			return;
		}
		if (client.isConnected()) {
			LOG.debug("Publishing message: {}", topic);
			try {
				client.publish(topic, payload, qos, retained);
				LOG.debug("Payload: {}", new String(payload, StandardCharsets.UTF_8));
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
			} catch (MqttException _) {
				// do nothing
			}
		}
		try {
			client.close();
		} catch (MqttException _) {
			// do nothing
		}
	}
	
	public void onMessage(OnMessageListener onMessageListener) {
		onMessageListeners.add(onMessageListener);
	}
	
	@FunctionalInterface
	public interface OnMessageListener {
		void onMessage(String topic, byte[] message);
	}
	
	public void onConnectionLost(OnConnectionLostListener onConnectionLostListener) {
		onConnectionLostListeners.add(onConnectionLostListener);
	}
	
	@FunctionalInterface
	public interface OnConnectionLostListener {
		void onConnectionLost(Throwable cause);
	}
}
