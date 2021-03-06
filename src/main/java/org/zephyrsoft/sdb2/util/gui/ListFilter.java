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
package org.zephyrsoft.sdb2.util.gui;

/**
 * A filter template for {@link TransparentFilterableListModel}.
 */
@FunctionalInterface
public interface ListFilter<T> {
	
	/**
	 * Check an object to see if it should be in the filtered list.
	 *
	 * @param object
	 *            the object to check
	 * @return {@code true} if the object should be in the filtered list; {@code false} otherwise
	 */
	boolean isAccepted(T object);
	
}
