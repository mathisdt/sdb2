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

/**
 * Indicates specific elements of a {@link Song}.
 */
public enum SongElementEnum {
	/** the title (if present, it is always exactly one line) */
	TITLE,
	/** a lyrics element (not always a whole line, see NEW_LINE) */
	LYRICS,
	/** a chord element (not always a whole line, see NEW_LINE) */
	CHORDS,
	/** a translation element (not always a whole line, see NEW_LINE) */
	TRANSLATION,
	/** a copyright element (always a whole line) */
	COPYRIGHT,
	/** indicates a line break between LYRICS, CHORDS and TRANSLATION elements - this element is only used there! */
	NEW_LINE;
}
