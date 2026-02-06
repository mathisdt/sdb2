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
 * Determines the format for an export.
 */
public class ExportFormat {
	private boolean showTranslation;
	private boolean showChords;
	private boolean onlyExportChordSequence;
	private boolean onlySongsWithChords;

	public ExportFormat(boolean showTranslation, boolean showChords, boolean onlyExportChordSequence, boolean onlySongsWithChords) {
		this.showTranslation = showTranslation;
		this.showChords = showChords;
        this.onlyExportChordSequence = onlyExportChordSequence;
        this.onlySongsWithChords = onlySongsWithChords;
	}
	
	public boolean isTranslationShown() {
		return showTranslation;
	}
	
	public boolean areChordsShown() {
		return showChords;
	}

	public boolean onlyExportChordSequence() {
		return onlyExportChordSequence;
	}

	public boolean onlySongsWithChords() {
		return onlySongsWithChords;
	}
}
