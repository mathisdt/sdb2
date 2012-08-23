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
import org.zephyrsoft.sdb2.model.Song;

/**
 * The representation of a {@link Song} for presentation.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PresentableSong implements Presentable {
	
	private Song song;
	
	public PresentableSong(Song song) {
		this.song = song;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getText()
	 */
	@Override
	public String getText() {
		// TODO respect display content selection in the settings: maybe cut out the chord lines
		return song.getLyrics();
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getBackground()
	 */
	@Override
	public Image getBackground() {
		// no image
		return null;
	}
	
}
