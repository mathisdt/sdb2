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

import java.util.List;

import javax.swing.*;

/**
 * A typed <b>mutable</b> combo box model implementation which transparently uses an underlying {@link List} (as inherited).
 */
public class TransparentMutableComboBoxModel<T> extends TransparentListModel<T> implements MutableComboBoxModel<T> {

	private static final long serialVersionUID = -1289734610645799530L;
	private final List<T> underlyingList;

	private Object selectedItem;

	public TransparentMutableComboBoxModel(List<T> underlyingList) {
		super(underlyingList);
		this.underlyingList = underlyingList;
	}
	
	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void addElement(final T item) {
		underlyingList.add(item);
	}

	@Override
	public void removeElement(final Object item) {
		underlyingList.remove(item);
	}

	@Override
	public void insertElementAt(final T item, final int index) {
		underlyingList.add(index, item);
	}

	@Override
	public void removeElementAt(final int index) {
		underlyingList.remove(index);
	}
}
