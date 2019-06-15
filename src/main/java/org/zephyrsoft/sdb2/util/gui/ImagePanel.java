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
package org.zephyrsoft.sdb2.util.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.border.Border;

import com.google.common.base.Preconditions;

/**
 * Display an image at the right scale to make it fit exactly into the container.
 */
public class ImagePanel extends JPanel {
	
	private static final long serialVersionUID = -1157676441033085598L;
	
	private Image image;
	private int lastUsedHeight = -1;
	private int lastUsedWidth = -1;
	private Image scaledImage;
	
	private float width;
	private float height;
	private int x;
	private int y;
	
	public ImagePanel(Image image) {
		this.image = image;
		
		// workaround for Nimbus L&F:
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 0));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (mustRescaleImage()) {
			calculateScaledImage();
		}
		if (scaledImage != null) {
			g.drawImage(scaledImage, x + (int) ((width - scaledImage.getWidth(null)) / 2), y
				+ (int) ((height - scaledImage.getHeight(null)) / 2), this);
		} else {
			throw new IllegalStateException("the scaled image was not computed");
		}
	}
	
	private boolean mustRescaleImage() {
		return lastUsedHeight != getHeight() || lastUsedWidth != getWidth();
	}
	
	private void calculateScaledImage() {
		Preconditions.checkArgument(image != null, "original image may not be null");
		
		// use floats so division below won't round
		float imageWidth = image.getWidth(null);
		float imageHeight = image.getHeight(null);
		width = this.getWidth();
		height = this.getHeight();
		x = 0;
		y = 0;
		
		// take special care for a border which might exist
		Border border = getBorder();
		if (border != null) {
			x = border.getBorderInsets(this).left;
			y = border.getBorderInsets(this).top;
			width -= x;
			width -= border.getBorderInsets(this).right;
			height -= y;
			height -= border.getBorderInsets(this).bottom;
		}
		
		if (width < imageWidth || height < imageHeight) {
			// decide which value should be taken to rescale the image
			if ((width / height) > (imageWidth / imageHeight)) {
				imageWidth = -1;
				imageHeight = height;
			} else {
				imageWidth = width;
				imageHeight = -1;
			}
			
			// prevent errors if panel is 0 wide or high
			if (imageWidth == 0) {
				imageWidth = -1;
			}
			if (imageHeight == 0) {
				imageHeight = -1;
			}
			
			scaledImage = image.getScaledInstance(Float.valueOf(imageWidth).intValue(), Float.valueOf(imageHeight).intValue(),
				Image.SCALE_DEFAULT);
			
		} else {
			scaledImage = image;
		}
	}
	
}
