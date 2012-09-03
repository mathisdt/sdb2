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

import java.util.ArrayList;
import java.util.List;
import org.zephyrsoft.util.StringTools;

/**
 * Parses a {@link Song} and thus prepares it for being shown in the editor or as presentation.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongParser {
	
	private SongParser() {
		// this class should only be used statically
	}
	
	/**
	 * Breaks down a {@link Song} into its elements. See {@link SongElementEnum} for more information about the line
	 * break policy used in the returned list!
	 * 
	 * @param song the song to parse
	 * @param includeTitle should the title be included?
	 * @param includeChords should all the chord lines be included?
	 * @return a list containing the elements, marked up using {@link SongElementEnum}s
	 */
	public static List<SongElement> parse(Song song, boolean includeTitle, boolean includeChords) {
		if (song == null) {
			throw new IllegalArgumentException("the song may not be null");
		}
		
		List<SongElement> ret = new ArrayList<>();
		
		// title
		if (includeTitle) {
			ret.add(new SongElement(SongElementEnum.TITLE, StringTools.nullAsEmptyString(song.getTitle())));
		}
		
		// lyrics
		if (song.getLyrics() != null) {
			boolean isFirst = true;
			for (String line : song.getLyrics().split("\n")) {
				if (isFirst) {
					isFirst = false;
				} else {
					ret.add(new SongElement(SongElementEnum.NEW_LINE, null));
				}
				// TODO
				
			}
		}
		
		// copyright
		if (!StringTools.isEmpty(song.getComposer())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, "Music: " + song.getComposer()));
		}
		if (!StringTools.isEmpty(song.getAuthorText())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, "Text: " + song.getAuthorText()));
		}
		if (!StringTools.isEmpty(song.getAuthorTranslation())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, "Translation: " + song.getAuthorTranslation()));
		}
		if (!StringTools.isEmpty(song.getPublisher())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, "Publisher: " + song.getPublisher()));
		}
		if (!StringTools.isEmpty(song.getAdditionalCopyrightNotes())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, song.getAdditionalCopyrightNotes()));
		}
		
		return ret;
	}
	
}
