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

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the behaviour of {@link SongParser}.
 *
 * @author Mathis Dirksen-Thedens
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
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
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
			+ SPACES_1 // index 11
			+ TRANSLATION_INTRO + TRANSLATION_2 + TRANSLATION_OUTRO // index 12
			+ SPACES_2 // index 13
		);
		// final newline after lyrics part (introduced by SongParser): index 14
		when(song.getComposer()).thenReturn(COMPOSER); // index 15
		when(song.getAuthorText()).thenReturn(AUTHOR_TEXT); // index 16
		when(song.getAuthorTranslation()).thenReturn(AUTHOR_TRANSLATION); // index 17
		when(song.getPublisher()).thenReturn(PUBLISHER); // index 18
		when(song.getAdditionalCopyrightNotes()).thenReturn(ADDITIONAL_NOTES); // index 19
	}
	
	@Test
	public void testParsing() {
		List<SongElement> result = SongParser.parse(song, true, true, true);
		Assert.assertEquals(20, result.size());
		
		Assert.assertEquals(new SongElement(SongElementEnum.TITLE, TITLE), result.get(0));
		
		Assert.assertEquals(new SongElement(SongElementEnum.CHORDS, CHORDS_1), result.get(1));
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(2));
		Assert.assertEquals(new SongElement(SongElementEnum.LYRICS, LYRICS_1), result.get(3));
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(4));
		Assert.assertEquals(new SongElement(SongElementEnum.TRANSLATION, TRANSLATION_1), result.get(5));
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(6));
		
		Assert.assertEquals(new SongElement(SongElementEnum.CHORDS, CHORDS_2), result.get(7));
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(8));
		Assert.assertEquals(new SongElement(SongElementEnum.LYRICS, LYRICS_2), result.get(9));
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(10));
		Assert.assertEquals(new SongElement(SongElementEnum.LYRICS, SPACES_1), result.get(11));
		Assert.assertEquals(new SongElement(SongElementEnum.TRANSLATION, TRANSLATION_2), result.get(12));
		Assert.assertEquals(new SongElement(SongElementEnum.LYRICS, SPACES_2), result.get(13));
		
		// final newline after lyrics part
		Assert.assertEquals(new SongElement(SongElementEnum.NEW_LINE, "\n"), result.get(14));
		
		Assert.assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_MUSIC + COMPOSER),
			result.get(15));
		Assert.assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_TEXT + AUTHOR_TEXT),
			result.get(16));
		Assert.assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_TRANSLATION
			+ AUTHOR_TRANSLATION), result.get(17));
		Assert.assertEquals(new SongElement(SongElementEnum.COPYRIGHT, SongParser.LABEL_PUBLISHER + PUBLISHER),
			result.get(18));
		Assert.assertEquals(new SongElement(SongElementEnum.COPYRIGHT, ADDITIONAL_NOTES), result.get(19));
		
	}
}
