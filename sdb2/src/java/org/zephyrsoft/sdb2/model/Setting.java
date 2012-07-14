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
package org.zephyrsoft.sdb2.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.zephyrsoft.util.StringTools;

/**
 * A global setting.
 * 
 * @author Mathis Dirksen-Thedens
 */
@XStreamAlias("setting")
public class Setting<T> implements Comparable<Setting<T>> {
	
	@XStreamAlias("key")
	private SettingKey key = null;
	@XStreamAlias("value")
	private T value = null;
	
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
		if (o == null) {
			return 1;
		} else {
			return StringTools.compareWithNullFirst(getKey().name(), o.getKey().name());
		}
	}
	
	@Override
	public String toString() {
		return "Setting[key=" + key + ",value=" + value + "]";
	}
	
}
