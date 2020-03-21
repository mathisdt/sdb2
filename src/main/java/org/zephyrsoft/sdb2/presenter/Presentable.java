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

import org.togglz.core.util.Preconditions;
import org.zephyrsoft.sdb2.model.Song;

/**
 * Something that can be presented on a digital projector: a song, an image or nothing (blank screen).
 */
public class Presentable {
	
	final Song song;
	final String image;
	
	public Presentable(Song song, String image) {
		Preconditions.checkArgument(song == null || image == null, "you cant't display a song and a logo image simultaneously, it's either or!");
		
		this.song = song;
		this.image = image;
	}
	
	/**
	 * Get the song.
	 */
	public Song getSong() {
		return song;
	}
	
	/**
	 * Get the image filename.
	 */
	public String getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return song != null
			? song.getTitle()
			: (image != null
				? image
				: "blank");
	}
	
}
