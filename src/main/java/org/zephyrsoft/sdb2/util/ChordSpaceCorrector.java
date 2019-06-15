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
package org.zephyrsoft.sdb2.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrects spacing in chord lines so the chords are positioned above the right lyrics.
 */
public class ChordSpaceCorrector {
	
	private static final Pattern CHORD_PATTERN = Pattern.compile("(\\s*+)\\b(\\S+)\\b");
	
	private Function<String, Integer> textToRenderedLength;
	
	/**
	 * @param textToRenderedLength
	 *            function to get the rendered (actual) length (in pixels) of any given text
	 */
	public ChordSpaceCorrector(Function<String, Integer> textToRenderedLength) {
		this.textToRenderedLength = textToRenderedLength;
	}
	
	public String correctChordSpaces(String chords, String lyrics) {
		try {
			StringBuilder result = new StringBuilder();
			Matcher matcher = CHORD_PATTERN.matcher(chords);
			while (matcher.find()) {
				String lyricsPart = lyrics.substring(0, matcher.end(1) > lyrics.length() ? lyrics.length() : matcher.end(1));
				while (textToRenderedLength.apply(lyricsPart) > textToRenderedLength.apply(result.toString() + " ")) {
					result.append(" ");
				}
				result.append(matcher.group(2)).append(" ");
			}
			return result.toString().replaceAll("\\s$", "");
		} catch (Exception e) {
			throw new IllegalStateException("problem while correcting chord spaces - chord line: '" + chords + "' - lyrics: '" + lyrics + "'", e);
		}
	}
	
}
