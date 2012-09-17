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
package org.zephyrsoft.sdb2.model;

import org.zephyrsoft.util.StringTools;

/**
 * Holds one element of a {@link Song}, e.g. one lyrics line or the title.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongElement {
	
	private SongElementEnum type;
	private String element;
	
	public SongElement(SongElementEnum type, String element) {
		this.type = type;
		this.element = element;
	}
	
	public SongElementEnum getType() {
		return type;
	}
	
	public String getElement() {
		return element;
	}
	
	@Override
	public String toString() {
		return type + (element != null ? "[" + element + "]" : "");
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SongElement && type == ((SongElement) obj).getType()
			&& StringTools.equals(element, ((SongElement) obj).getElement());
	}
	
}
