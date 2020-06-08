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

/**
 * 
 */
public class SongPosition {
	private int part;
	private int line;
	
	public SongPosition(int part) {
		this(part, 0);
	}
	
	public SongPosition(int part, int line) {
		this.part = part;
		this.line = line;
	}
	
	public int getPart() {
		return part;
	}
	
	public int getLine() {
		return line;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(part).append(',').append(line).toString();
	}
	
	public static SongPosition parseSongPosition(String s) {
		String[] parts = s.split(",");
		return new SongPosition(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SongPosition) {
			return this.part == ((SongPosition) obj).getPart() && this.line == ((SongPosition) obj).getLine();
		} else {
			return false;
		}
	}
}
