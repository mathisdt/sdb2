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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.util.StringTools;
import org.zephyrsoft.sdb2.util.gui.ImageTools;

/**
 * List entry for a {@link Song}.
 */
public class SongCell extends JPanel {
	private static final Logger LOG = LoggerFactory.getLogger(SongCell.class);
	
	private static final long serialVersionUID = 6861947343987825552L;
	public static final int TITLE_BOTTOM_SPACE = 5;
	public static final int FIRSTLINE_BOTTOM_SPACE = 2;
	
	private JLabel songTitle;
	private JLabel firstLine;
	private JLabel image;
	
	/**
	 * Constructor.
	 *
	 * @param leftSpace
	 *            space to insert left of the second line, in pixels
	 */
	public SongCell(Integer leftSpace) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);
		
		image = new JLabel("<IMG>");
		GridBagConstraints gbc_image = new GridBagConstraints();
		gbc_image.gridheight = 2;
		gbc_image.insets = new Insets(0, 0, 0, 10);
		gbc_image.gridx = 0;
		gbc_image.gridy = 0;
		add(image, gbc_image);
		// invisible when no image is explicitly set:
		image.setVisible(false);
		
		songTitle = new JLabel("<SONG TITLE>");
		songTitle.setBorder(new EmptyBorder(2, 3, 0, 3));
		GridBagConstraints gbcSongTitle = new GridBagConstraints();
		gbcSongTitle.insets = new Insets(0, 0, TITLE_BOTTOM_SPACE, 0);
		gbcSongTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcSongTitle.anchor = GridBagConstraints.NORTH;
		gbcSongTitle.gridx = 1;
		gbcSongTitle.gridy = 0;
		add(songTitle, gbcSongTitle);
		
		firstLine = new JLabel("<FIRST LINE>");
		firstLine.setBorder(new EmptyBorder(0, 23, FIRSTLINE_BOTTOM_SPACE, 3));
		if (leftSpace != null) {
			firstLine.setBorder(new EmptyBorder(0, leftSpace + 3, FIRSTLINE_BOTTOM_SPACE, 3));
		}
		firstLine.setFont(new Font("SansSerif", Font.ITALIC, 10));
		GridBagConstraints gbcFirstLine = new GridBagConstraints();
		gbcFirstLine.anchor = GridBagConstraints.NORTH;
		gbcFirstLine.fill = GridBagConstraints.HORIZONTAL;
		gbcFirstLine.gridx = 1;
		gbcFirstLine.gridy = 1;
		add(firstLine, gbcFirstLine);
	}
	
	public String getSongTitle() {
		return songTitle.getText();
	}
	
	public void setSongTitle(String text) {
		if (StringTools.isEmpty(text)) {
			// prevent a 0 pixel height:
			songTitle.setText(" ");
		} else {
			songTitle.setText(text);
		}
	}
	
	public String getFirstLine() {
		return firstLine.getText();
	}
	
	public void setFirstLine(String text) {
		if (StringTools.isEmpty(text)) {
			// prevent a 0 pixel height:
			firstLine.setText(" ");
		} else {
			firstLine.setText(text);
		}
	}
	
	public void setImage(final String imageUrl, int degreesToRotateRight) {
		if (imageUrl == null) {
			this.image.setVisible(false);
		} else {
			try {
				ImageIcon imageIcon = new ImageIcon(URI.create(imageUrl).toURL());
				Image image = imageIcon.getImage();
				image = ImageTools.rotate(image, degreesToRotateRight);
				double factor = (songTitle.getPreferredSize().getHeight() + firstLine.getPreferredSize().getHeight()
					+ TITLE_BOTTOM_SPACE + FIRSTLINE_BOTTOM_SPACE) * 2 / image.getHeight(null);
				image = ImageTools.scale(image, factor);
				this.image.setIcon(new ImageIcon(image));
				this.image.setText("");
				this.image.setVisible(true);
			} catch (MalformedURLException e) {
				this.image.setVisible(false);
                LOG.warn("could not locate image {}", imageUrl, e);
            }
        }
	}
	
	@Override
	public void setForeground(Color color) {
		if (songTitle != null && firstLine != null) {
			songTitle.setForeground(color);
			firstLine.setForeground(color);
		}
	}
	
}
