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
package org.zephyrsoft.util.gui;

import org.apache.commons.lang3.StringUtils;
import org.zephyrsoft.sdb2.gui.SongCell;

/**
 * Highlights parts of {@link SongCell} content.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongCellHighlighter {
	
	private String findRegex;
	private String replaceRegex;
	
	public void highlight(SongCell songCell) {
		if (StringUtils.isNoneBlank(findRegex, replaceRegex)) {
			songCell.setSongTitle(songCell.getSongTitle().replaceAll(findRegex, replaceRegex));
			songCell.setFirstLine(songCell.getFirstLine().replaceAll(findRegex, replaceRegex));
		}
	}
	
	public String getFindRegex() {
		return findRegex;
	}
	
	public void setFindRegex(String findRegex) {
		this.findRegex = findRegex;
	}
	
	public String getReplaceRegex() {
		return replaceRegex;
	}
	
	public void setReplaceRegex(String replaceRegex) {
		this.replaceRegex = replaceRegex;
	}
	
}
