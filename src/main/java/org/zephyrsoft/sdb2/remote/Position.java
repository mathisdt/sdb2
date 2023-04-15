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

import static org.zephyrsoft.sdb2.presenter.PresentationPosition.forSong;

import org.zephyrsoft.sdb2.model.Persistable;
import org.zephyrsoft.sdb2.presenter.PresentationPosition;
import org.zephyrsoft.sdb2.presenter.SongPresentationPosition;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Position implements Persistable {
	
	@XmlType
	@XmlEnum(String.class)
	public enum Visibility {
		@XmlEnumValue(value = "visible")
		VISIBLE,
		@XmlEnumValue(value = "blank")
		BLANK;
	}
	
	@XmlElement(name = "uuid")
	private String uuid;
	@XmlElement(name = "part")
	private int part;
	@XmlElement(name = "line")
	private int line;
	@XmlElement(name = "visibility")
	private Visibility visibility;
	
	public Position() {
		initIfNecessary();
	}
	
	/**
	 * Part: 0 = Title, 1-N Parts
	 */
	public Position(String uuid, int part) {
		this(uuid, part, 0, null);
	}
	
	public Position(String uuid, int part, int line) {
		this(uuid, part, line, null);
	}
	
	public Position(String uuid, PresentationPosition presentationPosition) {
		this(uuid,
			forSong(presentationPosition).map(SongPresentationPosition::getPartIndex).orElse(0),
			forSong(presentationPosition).map(SongPresentationPosition::getLineIndex).orElse(0),
			null);
	}
	
	public Position(String uuid, int part, int line, Visibility visibility) {
		this();
		this.uuid = uuid;
		this.part = part;
		this.line = line;
		if (visibility == Visibility.VISIBLE)
			this.visibility = null;
		else
			this.visibility = visibility;
	}
	
	public Position(Position p, Visibility visibility) {
		this(p != null ? p.uuid : null,
			p != null ? p.part : 0,
			p != null ? p.line : 0, visibility);
	}
	
	public Position(Position p) {
		this(p.uuid, p.part, p.line, p.visibility);
	}
	
	public int getPart() {
		return part;
	}
	
	public int getLine() {
		return line;
	}
	
	public boolean isVisible() {
		return visibility == null || visibility == Visibility.VISIBLE;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position pos) {
			return ((this.uuid == null && pos.getUUID() == null)
				|| (this.uuid != null && this.uuid.equals(pos.getUUID())))
				&& this.part == pos.getPart()
				&& this.line == pos.getLine()
				&& this.visibility == pos.getVisibility();
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
