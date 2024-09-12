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

import org.zephyrsoft.sdb2.model.Persistable;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fileSetResponse")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class FileSetResponse implements Persistable {
	private static final long serialVersionUID = 8867365661007188776L;
	
	@XmlElement(name = "uuid")
	private String uuid = null;
	@XmlElement(name = "ok")
	private boolean ok = true;
	@XmlElement(name = "reason")
	private String reason = "";
	
	public FileSetResponse() {
		initIfNecessary();
	}
	
	@Override
	public void initIfNecessary() {
		// Nothing to do
	}

	public boolean isOk() {
		return ok;
	}

	public String getUuid() {
		return uuid;
	}

	public String getReason() {
		return reason;
	}
}
