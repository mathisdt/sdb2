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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * A panel with a base color that can be translucent.
 */
public class TranslucentPanel extends JComponent {
	private static final long serialVersionUID = -3042727903698596561L;
	
	private Color baseColor;
	private int alpha;
	
	/** Create a completely transparent panel. */
	public TranslucentPanel(Color baseColor) {
		this.baseColor = baseColor;
		alpha = 0;
		setOpaque(false);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(translucentColor());
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	public Color getBaseColor() {
		return baseColor;
	}
	
	public void setBaseColor(Color baseColor) {
		this.baseColor = baseColor;
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	/**
	 * @param alpha
	 *            from 0 (completely transparent) to 255 (completely opaque)
	 */
	public void setAlpha(int alpha) {
		if (alpha == 0 || alpha == 255 || Math.abs(alpha - this.alpha) > 5) {
			this.alpha = alpha;
			repaint(40);
		}
	}
	
	private Color translucentColor() {
		return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
	}
}
