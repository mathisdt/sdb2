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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zephyrsoft.sdb2.util.StringTools;

import com.google.common.base.Preconditions;

/**
 * Parses a {@link Song} and thus prepares it for being shown in the editor or as presentation.
 *
 * @author Mathis Dirksen-Thedens
 */
public class SongParser {
	
	protected static final String LABEL_MUSIC = "Music: ";
	protected static final String LABEL_TEXT = "Text: ";
	protected static final String LABEL_TRANSLATION = "Translation: ";
	protected static final String LABEL_PUBLISHER = "Publisher: ";
	
	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("^(.*)\\[(.*)\\](.*)$");
	private static final String NEWLINE_REGEX = "\r?+\n";
	
	private SongParser() {
		// this class should only be used statically
	}
	
	/**
	 * Breaks down a {@link Song} into its elements. See {@link SongElementEnum} for more information about the line
	 * break policy used in the returned list!
	 *
	 * @param song
	 *            the song to parse
	 * @param includeTitle
	 *            should the title be included?
	 * @param includeChords
	 *            should all the chord lines be included?
	 * @return a list containing the elements, marked up using {@link SongElementEnum}s
	 */
	public static List<SongElement> parse(Song song, boolean includeTranslation, boolean includeTitle, boolean includeChords) {
		Preconditions.checkArgument(song != null, "song may not be null");
		
		List<SongElement> ret = new ArrayList<>();
		
		// title
		if (includeTitle) {
			ret.add(new SongElement(SongElementEnum.TITLE, StringTools.nullAsEmptyString(song.getTitle())));
		}
		
		// lyrics
		if (song.getLyrics() != null) {
			boolean isFirst = true;
			for (String line : song.getLyrics().split(NEWLINE_REGEX)) {
				Matcher translationMatcher = TRANSLATION_PATTERN.matcher(line);
				if (translationMatcher.matches()) {
					if (includeTranslation) {
						isFirst = addNewlineIfNotFirstLine(ret, isFirst);
						String prefix = translationMatcher.group(1);
						String translation = translationMatcher.group(2);
						String suffix = translationMatcher.group(3);
						if (!StringTools.isEmpty(prefix)) {
							ret.add(new SongElement(SongElementEnum.LYRICS, prefix));
						}
						if (!StringTools.isEmpty(translation)) {
							ret.add(new SongElement(SongElementEnum.TRANSLATION, translation));
						}
						if (!StringTools.isEmpty(suffix)) {
							ret.add(new SongElement(SongElementEnum.LYRICS, suffix));
						}
					}
				} else if (isChordsLine(line)) {
					if (includeChords) {
						isFirst = addNewlineIfNotFirstLine(ret, isFirst);
						ret.add(new SongElement(SongElementEnum.CHORDS, line));
					}
				} else {
					isFirst = addNewlineIfNotFirstLine(ret, isFirst);
					if (!line.isEmpty()) {
						ret.add(new SongElement(SongElementEnum.LYRICS, line));
					}
				}
				
			}
		}
		// always make sure there's a newline element after the lyrics part
		if (!ret.isEmpty() && ret.get(ret.size() - 1).getType() != SongElementEnum.NEW_LINE) {
			ret.add(newLineElement());
		}
		
		// copyright
		if (!StringTools.isEmpty(song.getComposer())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_MUSIC + song.getComposer()));
		}
		if (!StringTools.isEmpty(song.getAuthorText())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_TEXT + song.getAuthorText()));
		}
		if (!StringTools.isEmpty(song.getAuthorTranslation())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_TRANSLATION + song.getAuthorTranslation()));
		}
		if (!StringTools.isEmpty(song.getPublisher())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, LABEL_PUBLISHER + song.getPublisher()));
		}
		if (!StringTools.isEmpty(song.getAdditionalCopyrightNotes())) {
			ret.add(new SongElement(SongElementEnum.COPYRIGHT, song.getAdditionalCopyrightNotes()));
		}
		
		return ret;
	}
	
	private static boolean addNewlineIfNotFirstLine(List<SongElement> elementList, boolean isFirst) {
		boolean ret = isFirst;
		if (ret) {
			ret = false;
		} else {
			elementList.add(newLineElement());
		}
		return ret;
	}
	
	private static SongElement newLineElement() {
		return new SongElement(SongElementEnum.NEW_LINE, "\n");
	}
	
	/**
	 * Extract the first lyrics-only line from a song.
	 */
	public static String getFirstLyricsLine(Song song) {
		Preconditions.checkArgument(song != null, "song may not be null");
		
		if (song.getLyrics() != null) {
			for (String line : song.getLyrics().split(NEWLINE_REGEX)) {
				Matcher translationMatcher = TRANSLATION_PATTERN.matcher(line);
				if (!translationMatcher.matches() && !isChordsLine(line)) {
					return line;
				}
			}
		}
		return "";
	}
	
	/**
	 * Determines if the given line contains only guitar chords.
	 */
	private static boolean isChordsLine(String line) {
		return percentOfSpaces(line) >= 0.5;
	}
	
	/**
	 * Calculates the percentage of spaces in the given string.
	 *
	 * @return a value between 0.0 and 1.0
	 */
	private static double percentOfSpaces(String toParse) {
		int spacesCount = 0;
		for (int i = 0; i < toParse.length(); i++) {
			if (toParse.substring(i, i + 1).equals(" ")) {
				spacesCount++;
			}
		}
		if (toParse.length() != 0) {
			return (double) spacesCount / (double) toParse.length();
		} else {
			return 0.0;
		}
	}
	
}
