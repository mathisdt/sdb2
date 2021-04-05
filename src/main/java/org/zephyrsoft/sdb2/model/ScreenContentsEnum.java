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

import org.apache.commons.text.WordUtils;

/**
 * Types of presentation screen contents.
 */
public enum ScreenContentsEnum {
	ONLY_LYRICS("only lyrics", false, false, false),
	LYRICS_AND_CHORDS("lyrics and chords", false, true, false),
	LYRICS_AND_CHORD_SEQUENCE("lyrics and chord sequence", false, false, true),
	LYRICS_AND_CHORDS_AND_CHORD_SEQUENCE("lyrics, chords and chord sequence", false, true, true),
	LYRICS_AND_TRANSLATION("lyrics and translation", true, false, false),
	LYRICS_AND_TRANSLATION_AND_CHORDS("lyrics, translation and chords", true, true, false),
	LYRICS_AND_TRANSLATION_AND_CHORD_SEQUENCE("lyrics, translation and chord sequence", true, false, true),
	LYRICS_AND_TRANSLATION_AND_CHORDS_AND_CHORD_SEQUENCE("lyrics, translation, chords and chord sequence", true, true, true);
	
	private final String description;
	private final boolean showTranslation;
	private final boolean showChords;
	private final boolean showChordSequence;
	
	private ScreenContentsEnum(String description, boolean showTranslation, boolean showChords, boolean showChordSequence) {
		this.description = description;
		this.showTranslation = showTranslation;
		this.showChords = showChords;
		this.showChordSequence = showChordSequence;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean shouldShowTranslation() {
		return showTranslation;
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
