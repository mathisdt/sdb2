/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.presenter;

import java.awt.Image;

/**
 * The representation of a logo.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PresentableImage implements Presentable {
	
	private Image logo;
	
	public PresentableImage(Image logo) {
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
