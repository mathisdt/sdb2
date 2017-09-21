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

import com.google.common.base.Preconditions;

/**
 * Represents a screen connected to the computer.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SelectableScreen {
	
	private final int index;
	
	/**
	 * @param index
	 *            0-based (screen 0 is the main display)
	 */
	public SelectableScreen(int index) {
		Preconditions.checkArgument(index >= 0);
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return description including a screen number - attention: this number is 1-based and not 0-based as the
	 *         {@link #index}, so the screen with index 1 will have a description like "screen 2"!
	 */
	public String getDescription() {
		return "Screen " + (index + 1) + (index == 0 ? " (main screen)" : "");
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
}
