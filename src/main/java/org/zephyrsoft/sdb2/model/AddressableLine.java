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
package org.zephyrsoft.sdb2.model;

/**
 * A line that can be addressed.
 */
public class AddressableLine implements Addressable {
	
	private String text;
	private int indentation;
	private Integer position;
	
	public AddressableLine(SongElement element, Integer position) {
		this.text = element == null || element.getContent() == null
			? ""
			: element.getContent();
		indentation = element == null
			? 0
			: element.getIndentation();
		this.position = position;
	}
	
	@Override
	public Integer getPosition() {
		return position;
	}
	
	public String getText() {
		return text;
	}
	
	public int getIndentation() {
		return indentation;
	}
	
	@Override
	public String toString() {
		return "LINE[indent=" + indentation + "][" + text + "]";
	}
	
}
