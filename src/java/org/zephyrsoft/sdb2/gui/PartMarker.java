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
package org.zephyrsoft.sdb2.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Marker for a whole part (in contrast to single lines of a part). In "active" state, an additional arrow is drawn.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PartMarker extends JComponent {
	private static final long serialVersionUID = 1079584644440926887L;
	
	private static final int MARKER_WIDTH = 20;
	
	private final Color color;
	private final int margin;
	private boolean drawArrow = false;
	
	public PartMarker(Color color, int margin) {
		this.color = color;
		this.margin = margin;
	}
	
	public void setActive(boolean active) {
		drawArrow = active;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(color);
		int markerStartX = getWidth() - MARKER_WIDTH - margin;
		int markerHeight = getHeight() - (2 * margin);
		g.fillRect(markerStartX, margin, MARKER_WIDTH, markerHeight);
		
		if (drawArrow) {
			// calculate position and size: use one third of the available space
			int arrowStartX = markerStartX / 3;
			int arrowWidthAndHeight = Math.min(getHeight(), arrowStartX);
			int arrowStartY = margin + ((markerHeight - arrowWidthAndHeight) / 2);
			g.fillPolygon(new int[] {arrowStartX, arrowStartX, arrowStartX + arrowWidthAndHeight}, new int[] {
				arrowStartY, arrowStartY + arrowWidthAndHeight, (int) (arrowStartY + arrowWidthAndHeight * 0.5)}, 3);
		}
	}
}
