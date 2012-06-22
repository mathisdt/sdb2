package org.zephyrsoft.sdb2.presenter;

import java.awt.Image;

/**
 * Something that can be presented on a digital projector.
 * 
 * @author Mathis Dirksen-Thedens
 */
public interface Presentable {
	
	/**
	 * Get the foreground text.
	 */
	String getText();
	
	/**
	 * Get the background image.
	 */
	Image getBackground();
	
}
