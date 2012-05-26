package org.zephyrsoft.sdb2.gui;

import javax.swing.JList;

/**
 * A {@link JList} without horizontal scrolling. All cell renderers are fixed to the width of the list viewport
 * (horizontal scrolling is disabled).
 * 
 * @author Mathis Dirksen-Thedens
 */
public class FixedWidthJList<T> extends JList<T> {
	
	private static final long serialVersionUID = -9119825400896096359L;
	
	@Override
	public int getFixedCellWidth() {
		// anything different from -1 will do
		return 0;
	}
	
}
