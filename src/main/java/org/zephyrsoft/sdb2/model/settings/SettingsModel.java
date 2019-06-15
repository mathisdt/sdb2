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

import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.zephyrsoft.sdb2.model.Persistable;

/**
 * Global settings of the Song Database.
 */
@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class SettingsModel implements Persistable {
	
	// TODO possible optimization: use a map to make read and write access faster (no for loop needed)
	@XmlElement(name = "setting")
	private SortedSet<Setting<Object>> store;
	
	public SettingsModel() {
		initIfNecessary();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(SettingKey key, Class<T> clazz) {
		for (Setting<Object> setting : store) {
			if (setting.getKey() == key) {
				if (setting.getValue() != null && !clazz.isAssignableFrom(setting.getValue().getClass())) {
					throw new ClassCastException("wrong setting type - wanted " + clazz.getName() + " but found "
						+ setting.getValue().getClass().getName());
				}
				return (T) setting.getValue();
			}
		}
		return null;
	}
	
	private Object get(SettingKey key) {
		for (Setting<Object> setting : store) {
			if (setting.getKey() == key) {
				return setting.getValue();
			}
		}
		return null;
	}
	
	@Override
	public final void initIfNecessary() {
		if (store == null) {
			store = new TreeSet<>();
		}
		doMigrationIfNecessary();
	}
	
	private void doMigrationIfNecessary() {
		clearIfString(SettingKey.SCREEN_1_DISPLAY);
		clearIfString(SettingKey.SCREEN_2_DISPLAY);
	}
	
	private void clearIfString(SettingKey key) {
		if (get(key) instanceof String) {
			// it should be an Integer
			put(key, null);
		}
	}
	
	public boolean isSet(SettingKey key) {
		for (Setting<Object> setting : store) {
			if (setting.getKey() == key) {
				return true;
			}
		}
		return false;
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
			store.add(new Setting<>(key, value));
		}
	}
	
}
