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
import java.util.Observer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.zephyrsoft.sdb2.remote.MQTT.MQTTMessage;

public class MqttObject<T> extends Observable implements Observer {
	private T t;
	private String subscriptionTopic = null;
	private String publishTopic = null;
	private boolean pubEqSub;
	private int qos;
	private boolean retained;
	private MQTT mqtt;
	private Function<String, T> toObject;
	private Function<T, String> toString;
	private BiPredicate<T, T> takeObject;
	private BiPredicate<T, T> objectEquals;
	
	public MqttObject(MQTT mqtt, T object, String subscriptionTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String publishTopic,
		Function<T, String> toString, int qos, boolean retained, BiPredicate<T, T> objectEquals) {
		System.out.println("New MqttObject: S: " + subscriptionTopic + " P: " + publishTopic);
		this.subscriptionTopic = subscriptionTopic;
		this.toObject = toObject;
		this.publishTopic = publishTopic;
		pubEqSub = subscriptionTopic != null && publishTopic != null && subscriptionTopic.equals(publishTopic);
		this.takeObject = takeObject;
		if (!pubEqSub && takeObject == null)
			this.takeObject = (told, tnew) -> true;
		this.toString = toString;
		if (toString == null)
			this.toString = (t) -> t.toString();
		this.qos = qos;
		this.retained = retained;
		this.t = object;
		this.objectEquals = objectEquals;
		if (this.objectEquals == null)
			this.objectEquals = (a, b) -> a.equals(b);
		
		connectTo(mqtt);
	}
	
	public MqttObject(MQTT mqtt, T object, String subscriptionTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String publishTopic,
		Function<T, String> toString) {
		this(mqtt, object, subscriptionTopic, toObject, takeObject, publishTopic, toString, 1, false, null);
	}
	
	public void connectTo(MQTT mqtt) {
		this.mqtt = mqtt;
		if (this.t != null)
			set(this.t);
		subscribe();
	}
	
	public void set(T t) {
		set(t, true);
	}
	
	private void set(T t, boolean publish) {
		if (t != this.t && !this.objectEquals.test(t, this.t)) {
			this.t = t;
			setChanged();
			notifyObservers(t);
			if (publish)
				publish();
		}
	}
	
	public T get() {
		return t;
	}
	
	private void publish() {
		if (publishTopic != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					mqtt.publish(publishTopic, toString.apply(t), qos, retained);
				}
			}).start();
		}
	}
	
	private void subscribe() {
		if (subscriptionTopic != null) {
			mqtt.subscribe(subscriptionTopic, qos);
			mqtt.addObserver(this);
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		MQTTMessage m = ((MQTTMessage) arg);
		if (m.getTopic().equals(subscriptionTopic)) {
			T t = toObject.apply(m.getMessage());
			if (!pubEqSub) {
				if (takeObject.test(this.t, t)) {
					set(t, true);
				}
			} else {
				// Don't republish:
				set(t, false);
			}
		}
	}
	
}
