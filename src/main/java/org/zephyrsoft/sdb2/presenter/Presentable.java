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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((song == null) ? 0 : song.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Presentable other = (Presentable) obj;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (song == null) {
			if (other.song != null)
				return false;
		} else if (!song.equals(other.song))
			return false;
		return true;
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
