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
