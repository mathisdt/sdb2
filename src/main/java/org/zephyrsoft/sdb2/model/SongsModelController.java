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
package org.zephyrsoft.sdb2.model;

import java.util.Collections;

public class SongsModelController {
	
	protected final SongsModel songs;
	
	public SongsModelController(SongsModel songs) {
		this.songs = songs;
	}
	
	public boolean updateSong(Song changedSong) {
		return updateSongs(Collections.singletonList(changedSong));
	}
	
	public boolean updateSongs(Iterable<Song> changedSongs) {
		return !songs.updateSongsByUUID(changedSongs).isEmpty();
	}
	
	public boolean removeSong(Song songToDelete) {
		return songs.removeSong(songToDelete);
	}
	
	public void update(SongsModel songsForUpdate) {
		songs.update(songsForUpdate);
	}
	
	public boolean save() {
		return true;
	}
	
	public boolean close() {
		// Called before replaced by another controller
		return true;
	}
}
