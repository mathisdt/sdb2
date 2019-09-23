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

import org.zephyrsoft.sdb2.util.StringTools;

/**
 * Holds one element of a {@link Song}, e.g. one lyrics line or the title.
 */
public class SongElement {
	
	private final SongElementEnum type;
	private String content;
	private int indentation;
	
	public SongElement(SongElementEnum type, String content) {
		this(type, content, 0);
	}
	
	public SongElement(SongElementEnum type, String content, int indentation) {
		this.type = type;
		this.content = content;
		this.indentation = indentation;
	}
	
	public SongElementEnum getType() {
		return type;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public int getIndentation() {
		return indentation;
	}
	
	public void setIndentation(int indentation) {
		this.indentation = indentation;
	}
	
	public boolean isEmpty() {
		return StringTools.isEmpty(getContent());
	}
	
	@Override
	public String toString() {
		return type
			+ (indentation != 0 ? "[indent=" + indentation + "]" : "")
			+ (isEmpty() ? "[" + content + "]" : "");
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SongElement && type == ((SongElement) obj).getType()
			&& StringTools.equals(content, ((SongElement) obj).getContent());
	}
	
}
