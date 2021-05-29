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
package org.zephyrsoft.sdb2.model.settings;

import org.zephyrsoft.sdb2.util.StringTools;
import org.zephyrsoft.sdb2.util.converter.SettingValueAdapter;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A global setting.
 */
@XmlRootElement(name = "setting")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Setting<T> implements Comparable<Setting<T>> {
	
	@XmlElement(name = "key")
	private SettingKey key = null;
	@XmlElement(name = "value")
	@XmlJavaTypeAdapter(SettingValueAdapter.class)
	private T value = null;
	
	/**
	 * CAUTION: every setting has to have a key and a (possibly null) value! This constructor is only necessary for
	 * unmarshalling from XML.
	 */
	public Setting() {
		// default constructor
	}
	
	public Setting(SettingKey key, T value) {
		this.key = key;
		this.value = value;
	}
	
	public SettingKey getKey() {
		return key;
	}
	
	public void setKey(SettingKey key) {
		this.key = key;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(Setting<T> o) {
		if (o == null || o.getKey() == null) {
			return 1;
		} else if (getKey() == null) {
			return -1;
		} else {
			return StringTools.compareWithNullFirst(getKey().name(), o.getKey().name());
		}
	}
	
	@Override
	public String toString() {
		return "Setting[key=" + key + ",value=" + value + "]";
	}
	
}
