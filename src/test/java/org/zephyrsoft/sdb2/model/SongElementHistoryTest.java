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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.zephyrsoft.sdb2.model.SongElementMatcher.is;
import static org.zephyrsoft.sdb2.model.SongElementMatcher.isOneOf;

import java.util.Iterator;

import org.junit.Test;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongElementHistory;

import com.google.common.collect.Lists;

public class SongElementHistoryTest {
	
	private static final SongElement TITLE = new SongElement(SongElementEnum.TITLE, "Test Song");
	private static final SongElement CHORDS = new SongElement(SongElementEnum.CHORDS, "C         F   G   C");
	private static final SongElement LYRICS = new SongElement(SongElementEnum.LYRICS, "test test one two three");
	private static final SongElement NEW_LINE = new SongElement(SongElementEnum.NEW_LINE, "\n");
	private static final SongElement TRANSLATION = new SongElement(SongElementEnum.TRANSLATION, "Test Test Eins Zwei Drei");
	private static final SongElement COPYRIGHT = new SongElement(SongElementEnum.COPYRIGHT, "copyright by Mr. X");
	
	public SongElementHistory historyFull() {
		return new SongElementHistory(Lists.newArrayList(
			TITLE,
			CHORDS, NEW_LINE, LYRICS, NEW_LINE, TRANSLATION, NEW_LINE,
			CHORDS, NEW_LINE, LYRICS, NEW_LINE, TRANSLATION, NEW_LINE,
			NEW_LINE,
			TRANSLATION, NEW_LINE,
			CHORDS, NEW_LINE, LYRICS, NEW_LINE, TRANSLATION, NEW_LINE,
			CHORDS, NEW_LINE, LYRICS, NEW_LINE, TRANSLATION,
			COPYRIGHT));
	}
	
	public SongElementHistory historyMinimal() {
		return new SongElementHistory(Lists.newArrayList(
			LYRICS, NEW_LINE,
			LYRICS, NEW_LINE,
			NEW_LINE,
			TRANSLATION, NEW_LINE,
			LYRICS, NEW_LINE,
			LYRICS, NEW_LINE));
	}
	
	public SongElementHistory historyQuirky() {
		return new SongElementHistory(Lists.newArrayList(
			TRANSLATION, NEW_LINE,
			LYRICS, TRANSLATION, NEW_LINE,
			TRANSLATION, NEW_LINE,
			TRANSLATION, TRANSLATION, LYRICS, NEW_LINE,
			NEW_LINE,
			NEW_LINE,
			NEW_LINE,
			NEW_LINE,
			TRANSLATION, NEW_LINE,
			LYRICS, TRANSLATION, NEW_LINE,
			TRANSLATION, LYRICS, TRANSLATION));
	}
	
	private void iterate(SongElementHistory history, int times) {
		Iterator<SongElement> iterator = history.iterator();
		for (int i = 0; i < times; i++) {
			iterator.next();
		}
	}
	
	@Test
	public void historyFullElementsFound() {
		SongElementHistory history = historyFull();
		iterate(history, 7);
		assertTrue(history.query()
			.without(SongElementEnum.NEW_LINE)
			.lastSeen(is(SongElementEnum.CHORDS), is(SongElementEnum.LYRICS), is(SongElementEnum.TRANSLATION))
			.end().isMatched());
	}
	
	@Test
	public void historyFullElementsFoundIncludingCurrent() {
		SongElementHistory history = historyFull();
		iterate(history, 8);
		assertTrue(history.queryIncludingCurrent()
			.without(SongElementEnum.NEW_LINE)
			.lastSeen(is(SongElementEnum.CHORDS), is(SongElementEnum.LYRICS), is(SongElementEnum.TRANSLATION), is(SongElementEnum.CHORDS))
			.end().isMatched());
	}
	
	@Test
	public void historyFullElementsNotFound() {
		SongElementHistory history = historyFull();
		iterate(history, 7);
		assertFalse(history.query()
			.without(SongElementEnum.NEW_LINE)
			.lastSeen(is(SongElementEnum.CHORDS), is(SongElementEnum.TRANSLATION))
			.end().isMatched());
	}
	
	@Test
	public void historyMinimalElementsFound() {
		SongElementHistory history = historyMinimal();
		iterate(history, 9);
		assertTrue(history.query()
			.without(SongElementEnum.NEW_LINE)
			.lastSeen(is(SongElementEnum.TRANSLATION), isOneOf(SongElementEnum.LYRICS))
			.end().isMatched());
	}
	
	@Test
	public void historyMinimalElementsNotFound() {
		SongElementHistory history = historyMinimal();
		iterate(history, 9);
		assertFalse(history.query()
			.lastSeen(is(SongElementEnum.TRANSLATION), isOneOf(SongElementEnum.LYRICS))
			.end().isMatched());
	}
	
	@Test
	public void historyQuirkyElementsFound() {
		SongElementHistory history = historyQuirky();
		iterate(history, 17);
		assertTrue(history.query()
			.without(SongElementEnum.NEW_LINE)
			.lastSeen(is(SongElementEnum.TRANSLATION), isOneOf(SongElementEnum.LYRICS), is(SongElementEnum.TRANSLATION))
			.end().isMatched());
	}
	
}
