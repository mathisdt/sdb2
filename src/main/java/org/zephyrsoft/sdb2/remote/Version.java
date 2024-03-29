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

import java.util.Date;

import org.zephyrsoft.sdb2.model.Persistable;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "version")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Version implements Persistable {
	@XmlElement(name = "id")
	private long id;
	@XmlElement(name = "ts")
	private Date timestamp;
	@XmlElement(name = "user")
	private String username;
	@XmlElement(name = "uuid")
	private String uuid;
	@XmlElement(name = "dbuuid")
	private String dbuuid;
	
	public Version() {
		initIfNecessary();
	}
	
	public long getID() {
		return id;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getDBUUID() {
		return dbuuid;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			Version cmp = ((Version) obj);
			return this.id == cmp.getID() && this.uuid.equals(cmp.getUUID()) && username.equals(cmp.getUsername())
				&& this.timestamp == cmp.getTimestamp() && this.dbuuid.equals(cmp.getDBUUID());
		} else {
			return false;
		}
	}
	
	@Override
	public void initIfNecessary() {
		// nothing to do
	}
}
