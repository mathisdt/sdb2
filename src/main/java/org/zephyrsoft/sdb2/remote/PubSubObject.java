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

public class PubSubObject<T> extends MqttObject<T> {
	
	public PubSubObject(MQTT mqtt, T object, String subTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String pubTopic,
		Function<T, String> toString) {
		super(mqtt, object, subTopic, toObject, takeObject, pubTopic, toString);
	}
	
	public PubSubObject(MQTT mqtt, T object, String subTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String pubTopic) {
		super(mqtt, object, subTopic, toObject, takeObject, pubTopic, null);
	}
	
	public PubSubObject(MQTT mqtt, T object, String subTopic, Function<String, T> toObject, String pubTopic, Function<T, String> toString) {
		super(mqtt, object, subTopic, toObject, null, pubTopic, toString);
	}
	
	public PubSubObject(MQTT mqtt, T object, String subTopic, Function<String, T> toObject, String pubTopic) {
		super(mqtt, object, subTopic, toObject, null, pubTopic, null);
	}
	
	public PubSubObject(MQTT mqtt, T object, String topic, Function<String, T> toObject, Function<T, String> toString) {
		super(mqtt, object, topic, toObject, null, topic, toString);
	}
	
	public PubSubObject(MQTT mqtt, T object, String topic, Function<String, T> toObject) {
		super(mqtt, object, topic, toObject, null, topic, null);
	}
	
	public PubSubObject(MQTT mqtt, String subTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String pubTopic,
		Function<T, String> toString) {
		super(mqtt, null, subTopic, toObject, takeObject, pubTopic, toString);
	}
	
	public PubSubObject(MQTT mqtt, String subTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String pubTopic) {
		super(mqtt, null, subTopic, toObject, takeObject, pubTopic, null);
	}
	
	public PubSubObject(MQTT mqtt, String subTopic, Function<String, T> toObject, String pubTopic, Function<T, String> toString) {
		super(mqtt, null, subTopic, toObject, null, pubTopic, toString);
	}
	
	public PubSubObject(MQTT mqtt, String subTopic, Function<String, T> toObject, String pubTopic) {
		super(mqtt, null, subTopic, toObject, null, pubTopic, null);
	}
	
	public PubSubObject(MQTT mqtt, String topic, Function<String, T> toObject, Function<T, String> toString) {
		super(mqtt, null, topic, toObject, null, topic, toString);
	}
	
	public PubSubObject(MQTT mqtt, String topic, Function<String, T> toObject) {
		super(mqtt, null, topic, toObject, null, topic, null);
	}
	
}
