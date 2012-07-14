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
package org.zephyrsoft.sdb2.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.zephyrsoft.sdb.structure.Structure;
import org.zephyrsoft.sdb2.model.LanguageEnum;
import org.zephyrsoft.sdb2.model.Song;

/**
 * Importer for songs from a SDB v1 database.
 * 
 * @author Mathis Dirksen-Thedens
 */
@SuppressWarnings("deprecation")
public class ImportFromSDBv1 implements Importer {
	
	@Override
	public List<Song> loadFromFile(File inputFile) {
		if (inputFile == null || !inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
			throw new IllegalArgumentException("cannot read the indicated file");
		}
		
		Structure imported = new Structure();
		try {
			imported.loadFromFile(inputFile);
		} catch (Exception e) {
			throw new IllegalStateException("cannot not import the songs", e);
		}
		
		List<Song> ret = new ArrayList<Song>();
		for (org.zephyrsoft.sdb.structure.Song oldSong : imported.getSongs()) {
			Song newSong = new Song();
			newSong.setTitle(oldSong.getTitel());
			newSong.setLyrics(oldSong.getText());
			newSong.setTonality(oldSong.getTonart());
			String additionalCopyrightNotes = oldSong.getCopyright();
			if (additionalCopyrightNotes != null) {
				additionalCopyrightNotes = additionalCopyrightNotes.replaceAll("\r", "").replaceAll("\n", " ");
			}
			newSong.setAdditionalCopyrightNotes(additionalCopyrightNotes);
			if (oldSong.getSprache() != null) {
				LanguageEnum language = null;
				if (oldSong.getSprache().equals("deutsch")) {
					language = LanguageEnum.GERMAN;
				} else if (oldSong.getSprache().equals("englisch")) {
					language = LanguageEnum.ENGLISH;
				} else if (oldSong.getSprache().equals("gemischt")) {
					language = LanguageEnum.MIXED;
				}
				newSong.setLanguage(language);
			}
			newSong.setSongNotes(oldSong.getBemerkungen());
			ret.add(newSong);
		}
		return ret;
	}
}
