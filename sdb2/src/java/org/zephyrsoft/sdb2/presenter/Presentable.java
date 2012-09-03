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
 * Something that can be presented on a digital projector: a song, an image or nothing (blank screen).
 * 
 * @author Mathis Dirksen-Thedens
 */
public class Presentable {
	
	final Song song;
	final Image image;
	
	public Presentable(Song song, Image image) {
		// check arguments
		if (song != null && image != null) {
			throw new IllegalArgumentException(
				"you cant't display a song and a logo image simultaneously, it's either or!");
		}
		
		this.song = song;
		this.image = image;
	}
	
	/**
	 * Get the song.
	 */
	Song getSong() {
		return song;
	}
	
	/**
	 * Get the logo image.
	 */
	Image getImage() {
		return image;
	}
	
}
