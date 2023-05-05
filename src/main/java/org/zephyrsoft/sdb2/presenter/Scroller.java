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
package org.zephyrsoft.sdb2.presenter;

import java.util.List;

import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.Song;

/**
 * Control the scrolling inside a {@link Song}.
 */
public interface Scroller {
	
	/**
	 * Check if this instance has any addressable parts. This is especially not the case if the corresponding screen is
	 * marked as unavailable.
	 */
	boolean hasParts();
	
	/**
	 * Fetch a list of all addressable parts.
	 */
	List<AddressablePart> getParts();
	
	/**
	 * Start the transition to a specific part of the {@link Presentable}. This method should return immediately, even
	 * if the transition is not finished yet!
	 */
	void moveToPart(Integer part);
	
	/**
	 * Start the transition to a specific text line of the {@link Song} in the {@link Presentable}. Only lyrics lines
	 * count for this method - translation, chord and blank lines are ignored! This method should return immediately,
	 * even if the transition is not finished yet!
	 */
	void moveToLine(Integer part, Integer line);
	
	/**
	 * Start the transition to a specific position in the {@link Presentable}. This method should return immediately,
	 * even
	 * if the transition is not finished yet!
	 */
	void moveTo(SongPresentationPosition position);
	
}
