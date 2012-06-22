package org.zephyrsoft.util.gui;

import java.util.List;
import javax.swing.ComboBoxModel;

/**
 * A typed combo box model implementation which transparently uses an underlying {@link List} (as inherited).
 * 
 * @author Mathis Dirksen-Thedens
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
