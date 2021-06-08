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

@XmlRootElement(name = "patchRequest")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class PatchRequest implements Persistable {
	private static final long serialVersionUID = 8867566661007188776L;
	
	@XmlElement(name = "id")
	private long id;
	@XmlElement(name = "toID")
	private Long toID = null;
	
	public PatchRequest() {
		initIfNecessary();
	}
	
	public PatchRequest(long id) {
		this();
		this.id = id;
		this.toID = null;
	}
	
	public PatchRequest(long id, long toID) {
		this();
		this.id = id;
		this.toID = toID;
	}
	
	@Override
	public void initIfNecessary() {
		id = 1;
		toID = null;
	}
}
