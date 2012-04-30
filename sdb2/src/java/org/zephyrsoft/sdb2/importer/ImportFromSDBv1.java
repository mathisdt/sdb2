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
