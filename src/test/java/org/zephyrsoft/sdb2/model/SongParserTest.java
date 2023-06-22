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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the behaviour of {@link SongParser}.
 */
public class SongParserTest {
	
	private static final String NEWLINE = "\n";
	private static final String TRANSLATION_OUTRO = "]";
	private static final String TRANSLATION_INTRO = "[";
	private static final String TITLE = "title";
	private static final String CHORDS_1 = "A     B     C";
	private static final String LYRICS_1 = "first lyrics line";
	private static final String TRANSLATION_1 = "translation 1";
	private static final String CHORDS_2 = "D    E    F";
	private static final String LYRICS_2 = "second lyrics line";
	private static final String SPACES_1 = "   ";
	private static final String TRANSLATION_2 = "translation 2";
	private static final String SPACES_2 = "                           ";
	private static final String COMPOSER = "composer";
	private static final String AUTHOR_TEXT = "author text";
	private static final String AUTHOR_TRANSLATION = "author translation";
	private static final String PUBLISHER = "publisher";
	private static final String ADDITIONAL_NOTES = "additional notes";
	
	@Mock
	private Song song;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		
		when(song.getTitle()).thenReturn(TITLE); // index 0
		when(song.getLyrics()).thenReturn(CHORDS_1 // index 1
			+ NEWLINE // index 2
			+ LYRICS_1 // index 3
			+ NEWLINE // index 4
			+ TRANSLATION_INTRO + TRANSLATION_1 + TRANSLATION_OUTRO // index 5
			+ NEWLINE // index 6
			+ CHORDS_2 // index 7
			+ NEWLINE // index 8
			+ LYRICS_2 // index 9
			+ NEWLINE // index 10
			+ SPACES_1 // no index, counted as indentation for following
			+ TRANSLATION_INTRO + TRANSLATION_2 + TRANSLATION_OUTRO // index 11
			+ SPACES_2 // no index, blank space after translation is trimmed
		);
		// final newline after lyrics part (introduced by SongParser): index 12
		when(song.getComposer()).thenReturn(COMPOSER); // index 13
		when(song.getAuthorText()).thenReturn(AUTHOR_TEXT); // index 14
		when(song.getAuthorTranslation()).thenReturn(AUTHOR_TRANSLATION); // index 15
		when(song.getPublisher()).thenReturn(PUBLISHER); // index 16
		when(song.getAdditionalCopyrightNotes()).thenReturn(ADDITIONAL_NOTES); // index 17
	}
	
	@Test
	public void testParsing() {
		List<SongElement> result = SongParser.parse(song, true, true, true);
		// Assert.assertEquals(20, result.size());
		
		assertEquals(new SongElement(SongElementEnum.TITLE, TITLE), result.get(0));
		
		assertEquals(new SongElement(SongElementEnum.CHORDS, CHORDS_1), result.get(1));
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(2));
		assertEquals(new SongElement(SongElementEnum.LYRICS, LYRICS_1), result.get(3));
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(4));
		assertEquals(new SongElement(SongElementEnum.TRANSLATION, TRANSLATION_1), result.get(5));
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(6));
		
		assertEquals(new SongElement(SongElementEnum.CHORDS, CHORDS_2), result.get(7));
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(8));
		assertEquals(new SongElement(SongElementEnum.LYRICS, LYRICS_2), result.get(9));
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(10));
		SongElement index11 = result.get(11);
		assertEquals(new SongElement(SongElementEnum.TRANSLATION, TRANSLATION_2), index11);
		assertEquals(SPACES_1.length(), index11.getIndentation());
		
		// final newline after lyrics part
		assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(12));
		
		assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_MUSIC + COMPOSER),
			result.get(13));
		assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_TEXT + AUTHOR_TEXT),
			result.get(14));
		assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_TRANSLATION
			+ AUTHOR_TRANSLATION), result.get(15));
		assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_PUBLISHER + PUBLISHER),
			result.get(16));
		assertEquals(new SongElement(SongElementEnum.COPYRIGHT, ADDITIONAL_NOTES), result.get(17));
		
	}
}
