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
package org.zephyrsoft.sdb2.remote;

import java.util.Objects;
import java.util.UUID;

import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;

/**
 * 
 */
public class SDB2RemotePreferences implements RemotePreferences {
	
	private String prefix;
	private String room;
	private String server;
	private String username;
	private String password;
	private String clientID;
	
	public SDB2RemotePreferences(SettingsModel settingsModel) {
		prefix = settingsModel.get(SettingKey.REMOTE_PREFIX, String.class);
		room = settingsModel.get(SettingKey.REMOTE_NAMESPACE, String.class);
		server = settingsModel.get(SettingKey.REMOTE_SERVER, String.class);
		username = settingsModel.get(SettingKey.REMOTE_USERNAME, String.class);
		password = settingsModel.get(SettingKey.REMOTE_PASSWORD, String.class);
		clientID = UUID.randomUUID().toString();
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getRoom()
	 */
	@Override
	public String getRoom() {
		return room;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getServer()
	 */
	@Override
	public String getServer() {
		return server;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getUsername()
	 */
	@Override
	public String getUsername() {
		return username;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getPassword()
	 */
	@Override
	public String getPassword() {
		return password;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.remote.RemotePreferences#getClientID()
	 */
	@Override
	public String getClientID() {
		return clientID;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SDB2RemotePreferences that = (SDB2RemotePreferences) o;
		return Objects.equals(getPrefix(), that.getPrefix()) && Objects.equals(getRoom(), that.getRoom()) && Objects.equals(getServer(), that
			.getServer()) && Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getPassword(), that.getPassword());
	}
}
