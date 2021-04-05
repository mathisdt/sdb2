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

import com.google.common.base.Preconditions;

/**
 * Represents a display connected to the computer.
 */
public class SelectableDisplay {
	
	private final int index;
	private boolean available;
	
	/**
	 * @param index
	 *            0-based (display 0 is the main display)
	 */
	public SelectableDisplay(int index) {
		this(index, true);
	}
	
	/**
	 * @param index
	 *            0-based (display 0 is the main display)
	 * @param available
	 *            is this display available at present?
	 */
	public SelectableDisplay(int index, boolean available) {
		Preconditions.checkArgument(index >= 0);
		this.index = index;
		this.available = available;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	/**
	 * @return description including a display number - attention: this number is 1-based and not 0-based as the
	 *         {@link #index}, so the display with index 1 will have a description like "display 2"!
	 */
	public String getDescription() {
		return "Display " + (index + 1) + (index == 0 ? " (main display)" : "") + (available ? "" : " - currently unavailable");
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectableDisplay other = (SelectableDisplay) obj;
		if (index != other.index)
			return false;
		return true;
	}
	
}
