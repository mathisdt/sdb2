package org.zephyrsoft.sdb2.presenter;

import java.awt.Image;

/**
 * The representation of an empty screen.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class DisplayBlank implements Presentable {
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getText()
	 */
	@Override
	public String getText() {
		// no text
		return null;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getBackground()
	 */
	@Override
	public Image getBackground() {
		// no logo
		return null;
	}
	
}
