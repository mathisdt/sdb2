package org.zephyrsoft.util.gui;

import java.util.List;
import javax.swing.AbstractListModel;

/**
 * A typed list model implementation which transparently uses an underlying {@link List}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class TransparentListModel<T> extends AbstractListModel<T> {
	
	private static final long serialVersionUID = -2952298254786461472L;
	
	private List<T> underlyingList = null;
	
	public TransparentListModel(List<T> underlyingList) {
		this.underlyingList = underlyingList;
	}
	
	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return underlyingList.size();
	}
	
	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public T getElementAt(int index) {
		return underlyingList.get(index);
	}
	
	public List<T> getAllElements() {
		return underlyingList;
	}
	
}
