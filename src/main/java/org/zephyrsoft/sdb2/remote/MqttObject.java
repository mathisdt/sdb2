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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttObject<T> {
	private T object;
	private String subscriptionTopic;
	private String publishTopic;
	private boolean pubEqSub;
	private int qos;
	private boolean retained;
	private MQTT mqtt;
	private Function<String, T> toObject;
	private Function<T, String> toString;
	private BiPredicate<T, T> takeObject;
	private BiPredicate<T, T> objectEquals;
	private CopyOnWriteArrayList<OnChangeListener<T>> onChangeListeners = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<OnChangeListener<T>> onRemoteChangeListeners = new CopyOnWriteArrayList<>();
	
	private static final Logger LOG = LoggerFactory.getLogger(MqttObject.class);
	
	/**
	 * A simple MQTT based property for synchronizing java objects.
	 *
	 * It can be used for Publish or Subscribe only, Publish&Subscribe on the same topic, or Publish&Subscribe on
	 * separate topics.
	 *
	 * A initially set object which is not null will be published before subscribe is called. Null objects are
	 * supported,
	 * if toObject, takeObject, toString and objectEquals are supporting it. Use case might be a retained message which
	 * can be null, if not set. toString must create a empty String to unset it. Attention: subcribers may not get the
	 * empty string.
	 *
	 * @throws MqttException
	 *
	 *
	 */
	public MqttObject(MQTT mqtt, T object, String subscriptionTopic, Function<String, T> toObject, BiPredicate<T, T> takeObject, String publishTopic,
		Function<T, String> toString, int qos, boolean retained, BiPredicate<T, T> objectEquals) throws MqttException {
		LOG.trace("new MqttObject: S: {} P: {}", subscriptionTopic, publishTopic);
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
		this.objectEquals = objectEquals;
		if (this.objectEquals == null)
			this.objectEquals = (a, b) -> a == null ? b == null : a.equals(b);
		this.object = object;
		
		mqtt.onMessage((topic, message) -> {
			if (topic.equals(subscriptionTopic)) {
				T newObject = toObject.apply(message);
				if (!pubEqSub && !takeObject.test(object, newObject))
					return;
				set(newObject, true);
			}
		});
		connectTo(mqtt);
	}
	
	public void connectTo(MQTT pMqtt) throws MqttException {
		mqtt = pMqtt;
		if (object != null)
			publish();
		subscribe();
	}
	
	public void set(T pObject) {
		set(pObject, false);
	}
	
	private void set(T pObject, boolean fromRemote) {
		if (pObject == object || objectEquals.test(pObject, object))
			return;
		object = pObject;
		onChangeListeners.forEach((ocl) -> ocl.onChange(pObject));
		if (fromRemote)
			onRemoteChangeListeners.forEach((ocl) -> ocl.onChange(pObject));
		else
			publish();
	}
	
	public T get() {
		return object;
	}
	
	private void publish() {
		if (publishTopic != null)
			new Thread(() -> mqtt.publish(publishTopic, toString.apply(object), qos, retained)).start();
	}
	
	private void subscribe() throws MqttException {
		if (subscriptionTopic != null) {
			mqtt.subscribe(subscriptionTopic, qos);
		}
	}
	
	public void onChange(OnChangeListener<T> onChangeListener) {
		onChangeListeners.add(onChangeListener);
	}
	
	public void onRemoteChange(OnChangeListener<T> onChangeListener) {
		onRemoteChangeListeners.add(onChangeListener);
	}
	
	public interface OnChangeListener<T> {
		public abstract void onChange(T object);
	}
	
}
