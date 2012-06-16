package org.zephyrsoft.sdb2.model;

import java.awt.Color;
import java.awt.Font;
import java.util.SortedSet;
import java.util.TreeSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Global settings of the Song Database.
 * 
 * @author Mathis Dirksen-Thedens
 */
@XStreamAlias("settings")
public class SettingsModel {
	
	@XStreamImplicit(itemFieldName = "setting")
	private SortedSet<Setting<Object>> store;
	
	public SettingsModel() {
		initIfNecessary();
	}
	
	public Object get(SettingKey key) {
		for (Setting<Object> setting : store) {
			if (setting.getKey() == key) {
				return setting.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Is called from the local constructor and from {@link XMLConverter} to ensure a valid inner state after conversion
	 * from XML and after creation via constructor. This is in this method because XStream might overwrite the value set
	 * inside the constructor with {@code null}.
	 */
	public final void initIfNecessary() {
		if (store == null) {
			store = new TreeSet<>();
		}
	}
	
	public String getString(SettingKey key) {
		Object value = get(key);
		if (key.getType() != String.class) {
			throw new ClassCastException("wrong setting type");
		}
		return (String) value;
	}
	
	public Integer getInteger(SettingKey key) {
		Object value = get(key);
		if (key.getType() != Integer.class) {
			throw new ClassCastException("wrong setting type");
		}
		return (Integer) value;
	}
	
	public Color getColor(SettingKey key) {
		Object value = get(key);
		if (key.getType() != Color.class) {
			throw new ClassCastException("wrong setting type");
		}
		return (Color) value;
	}
	
	public Font getFont(SettingKey key) {
		Object value = get(key);
		if (key.getType() != Font.class) {
			throw new ClassCastException("wrong setting type");
		}
		return (Font) value;
	}
	
	public void put(SettingKey key, Object value) {
		Setting<Object> toSet = null;
		for (Setting<Object> setting : store) {
			if (setting.getKey() == key) {
				toSet = setting;
				break;
			}
		}
		if (toSet != null) {
			toSet.setValue(value);
		} else {
			store.add(new Setting<Object>(key, value));
		}
	}
	
}
