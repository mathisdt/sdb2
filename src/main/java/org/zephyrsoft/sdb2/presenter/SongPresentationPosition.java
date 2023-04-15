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
package org.zephyrsoft.sdb2.presenter;

/**
 * Indicates a position inside a song.
 */
public class SongPresentationPosition implements PresentationPosition {
	/** index of a part */
	private final Integer partIndex;
	/** index of a line inside the part indicated by {@link #partIndex} (can be {@code null}) */
	private final Integer lineIndex;
	
	public SongPresentationPosition(Integer partIndex, Integer lineIndex) {
		this.partIndex = partIndex;
		this.lineIndex = lineIndex;
	}
	
	public Integer getPartIndex() {
		return partIndex;
	}
	
	public Integer getLineIndex() {
		return lineIndex;
	}
	
	@Override
	public String toString() {
		return "SongPresentationPosition [partIndex=" + partIndex + ", lineIndex=" + lineIndex + "]";
	}
	
}
