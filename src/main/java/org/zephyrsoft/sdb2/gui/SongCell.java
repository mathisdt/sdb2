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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.util.StringTools;

/**
 * List entry for a {@link Song}.
 */
public class SongCell extends JPanel {
	
	private static final long serialVersionUID = 6861947343987825552L;
	
	private JLabel songTitle;
	private JLabel firstLine;
	
	/**
	 * Constructor.
	 *
	 * @param leftSpace
	 *            space to insert left of the second line, in pixels
	 */
	public SongCell(Integer leftSpace) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);
		
		songTitle = new JLabel("<SONG TITLE>");
		songTitle.setBorder(new EmptyBorder(2, 3, 0, 3));
		GridBagConstraints gbcSongTitle = new GridBagConstraints();
		gbcSongTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcSongTitle.anchor = GridBagConstraints.NORTH;
		gbcSongTitle.gridx = 0;
		gbcSongTitle.gridy = 0;
		add(songTitle, gbcSongTitle);
		
		firstLine = new JLabel("<FIRST LINE>");
		firstLine.setBorder(new EmptyBorder(0, 23, 2, 3));
		if (leftSpace != null) {
			firstLine.setBorder(new EmptyBorder(0, leftSpace + 3, 2, 3));
		}
		firstLine.setFont(new Font("SansSerif", Font.ITALIC, 10));
		GridBagConstraints gbcFirstLine = new GridBagConstraints();
		gbcFirstLine.anchor = GridBagConstraints.NORTH;
		gbcFirstLine.fill = GridBagConstraints.HORIZONTAL;
		gbcFirstLine.gridx = 0;
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
	
	@Override
	public void setForeground(Color color) {
		if (songTitle != null && firstLine != null) {
			songTitle.setForeground(color);
			firstLine.setForeground(color);
		}
	}
	
}
