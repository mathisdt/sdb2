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

import javax.swing.ComboBoxModel;

/**
 * A typed combo box model implementation which transparently uses an underlying {@link List} (as inherited).
 */
public class TransparentComboBoxModel<T> extends TransparentListModel<T> implements ComboBoxModel<T> {
	
	private static final long serialVersionUID = -1289734610645799530L;
	
	private Object selectedItem;
	
	public TransparentComboBoxModel(List<T> underlyingList) {
		super(underlyingList);
	}
	
	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}
	
	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}
	
}
