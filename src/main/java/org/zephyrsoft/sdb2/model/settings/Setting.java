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
package org.zephyrsoft.sdb2.model.settings;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.zephyrsoft.sdb2.util.StringTools;
import org.zephyrsoft.sdb2.util.converter.SettingValueAdapter;

/**
 * A global setting.
 * 
 * @author Mathis Dirksen-Thedens
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
