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
package org.zephyrsoft.sdb2.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * Marker for a whole part (in contrast to single lines of a part). In "active" state, an additional arrow is drawn.
 */
public class PartMarker extends JComponent {
	private static final long serialVersionUID = 1079584644440926887L;
	
	private static final int MARKER_WIDTH = 20;
	
	private final Color color;
	private final int margin;
	private PartButtonGroup partButtonGroup;
	private boolean drawArrow = false;
	
	public PartMarker(Color color, int margin, PartButtonGroup partButtonGroup) {
		this.color = color;
		this.margin = margin;
		this.partButtonGroup = partButtonGroup;
	}
	
	/** for mouse-over effect */
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
		
		// TODO paint marker if partButtonGroup.isActive() at line partButtonGroup.getActiveLine()
		
		if (drawArrow) {
			// calculate position and size: use one third of the available space
			int arrowStartX = markerStartX / 3;
			int arrowWidthAndHeight = Math.min(getHeight(), arrowStartX);
			int arrowStartY = margin + ((markerHeight - arrowWidthAndHeight) / 2);
			g.fillPolygon(new int[] { arrowStartX, arrowStartX, arrowStartX + arrowWidthAndHeight }, new int[] {
				arrowStartY, arrowStartY + arrowWidthAndHeight, (int) (arrowStartY + arrowWidthAndHeight * 0.5) }, 3);
		}
	}
}
