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

import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.MqttException;

public class SubObject<T> extends MqttObject<T> {
	
	public SubObject(MQTT mqtt, T object, String subscriptionTopic, Function<String, T> toObject) throws MqttException {
		super(mqtt, object, subscriptionTopic, toObject, null, null, null, 0, false, null);
	}
	
	public SubObject(MQTT mqtt, String subscriptionTopic, Function<String, T> toObject) throws MqttException {
		super(mqtt, null, subscriptionTopic, toObject, null, null, null, 0, false, null);
	}
}
