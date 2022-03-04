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

@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Position implements Persistable {
	
	@XmlElement(name = "uuid")
	private String uuid;
	@XmlElement(name = "part")
	private int part;
	@XmlElement(name = "line")
	private int line;
	
	public Position() {
		initIfNecessary();
	}
	
	/**
	 * Part: 0 = Title, 1-N Parts
	 */
	public Position(String uuid, int part) {
		this(uuid, part, 0);
	}
	
	public Position(String uuid, int part, int line) {
		this();
		this.uuid = uuid;
		this.part = part;
		this.line = line;
	}
	
	public Position(String uuid, int part, int line, boolean partWithTitle) {
		this(uuid, partWithTitle ? part : part + 1, line);
	}
	
	public int getPart() {
		return part;
	}
	
	public int getPart(boolean withTitle) {
		return withTitle ? part : part - 1;
	}
	
	public int getLine() {
		return line;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			return this.uuid.equals(((Position) obj).getUUID()) && this.part == ((Position) obj).getPart() && this.line == ((Position) obj).getLine();
		} else {
			return false;
		}
	}
	
	public String getUUID() {
		return uuid;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.model.Persistable#initIfNecessary()
	 */
	@Override
	public void initIfNecessary() {
		// nothing to do
	}
}
