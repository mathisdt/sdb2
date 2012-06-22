package org.zephyrsoft.sdb2.presenter;

import java.awt.Image;

/**
 * The representation of a logo.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class DisplayLogo implements Presentable {
	
	private Image logo;
	
	public DisplayLogo(Image logo) {
		this.logo = logo;
	}
	
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
		return logo;
	}
	
}
