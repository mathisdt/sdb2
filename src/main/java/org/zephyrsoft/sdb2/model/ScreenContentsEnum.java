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

import org.apache.commons.text.WordUtils;

/**
 * Types of presentation screen contents.
 *
 * @author Mathis Dirksen-Thedens
 */
public enum ScreenContentsEnum {
	ONLY_LYRICS("only lyrics", true, false, false),
	LYRICS_AND_CHORDS("lyrics and chords", true, true, false),
	LYRICS_AND_CHORD_SEQUENCE("lyrics and chord sequence", true, false, true),
	LYRICS_AND_CHORDS_AND_CHORD_SEQUENCE("lyrics, chords and chord sequence", true, true, true);
	
	private final String description;
	private final boolean showLyrics;
	private final boolean showChords;
	private final boolean showChordSequence;
	
	private ScreenContentsEnum(String description, boolean showLyrics, boolean showChords, boolean showChordSequence) {
		this.description = description;
		this.showLyrics = showLyrics;
		this.showChords = showChords;
		this.showChordSequence = showChordSequence;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean shouldShowLyrics() {
		return showLyrics;
	}
	
	public boolean shouldShowChords() {
		return showChords;
	}
	
	public boolean shouldShowChordSequence() {
		return showChordSequence;
	}
	
	public String getInternalName() {
		return WordUtils.capitalizeFully(name(), new char[] { '_' }).replaceAll("_", "");
	}
	
	public static ScreenContentsEnum withInternalName(String internalName) {
		for (ScreenContentsEnum sce : values()) {
			if (sce.getInternalName().equalsIgnoreCase(internalName)) {
				return sce;
			}
		}
		return null;
	}
}
