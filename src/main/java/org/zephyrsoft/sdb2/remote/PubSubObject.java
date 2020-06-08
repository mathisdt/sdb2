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

import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.MqttException;

public class PubSubObject<T> extends MqttObject<T> {
	
	public PubSubObject(MQTT mqtt, String topic, Function<String, T> toObject, boolean retained) throws MqttException {
		super(mqtt, null, topic, toObject, null, topic, null, 0, retained, null);
	}
	
	public PubSubObject(MQTT mqtt, String topic, Function<String, T> toObject, Function<T, String> toString, boolean retained) throws MqttException {
		super(mqtt, null, topic, toObject, null, topic, toString, 0, retained, null);
	}
	
	public PubSubObject(MQTT mqtt, String topic, Function<String, T> toObject, Function<T, String> toString, boolean retained,
		BiPredicate<T, T> objectEquals) throws MqttException {
		super(mqtt, null, topic, toObject, null, topic, toString, 0, retained, objectEquals);
	}
}
