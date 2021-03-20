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

import java.text.DateFormat;
import java.text.ParseException;

/**
 *
 */
public class PatchVersion {
	private final long id;
	private final long timestamp;
	private final String username;
	private final String hash;
	
	public PatchVersion(long id, long timestamp, String username, String hash) {
		this.id = id;
		this.timestamp = timestamp;
		this.username = username;
		this.hash = hash;
	}
	
	public long getId() {
		return id;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(id).append(',').append(timestamp).append(',').append(username).append(',').append(hash).toString();
	}
	
	public static PatchVersion parsePatchVersion(String s) {
		String[] parts = s.split(",");
		long ts = 0;
		try {
			ts = DateFormat.getInstance().parse(parts[1]).getTime();
		} catch (ParseException ignored) {
			
		}
		return new PatchVersion(Integer.parseInt(parts[0]), ts, parts[2], parts[3]);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PatchVersion) {
			PatchVersion cmp = ((PatchVersion) obj);
			return this.id == cmp.getID() && this.hash.equals(cmp.getHash()) && username.equals(cmp.getUsername())
				&& this.timestamp == cmp.getTimestamp();
		} else {
			return false;
		}
	}
	
	private long getID() {
		return id;
	}
}
