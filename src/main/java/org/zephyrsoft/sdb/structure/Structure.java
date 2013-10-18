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
package org.zephyrsoft.sdb.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Vector;

/**
 * Structure which holds all songs.
 * 
 * @deprecated This is a LEGACY class from SDB v1 which was stripped down to the absolutely necessary bits!
 * @author Mathis Dirksen-Thedens
 */
@Deprecated
public class Structure {
	
	private Vector<Song> songs = new Vector<Song>();
	
	public List<Song> getSongs() {
		return songs;
	}
	
	public int getSongCount() {
		return songs.size();
	}
	
	public Song getSongAt(int index) {
		return songs.get(index);
	}
	
	public void loadFromFile(File file) throws IOException, ClassNotFoundException {
		// load from a file which contains the serialized Vector of Songs
		if (file.exists() && file.isFile()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			songs = (Vector<Song>) in.readObject();
			// don't read any more data (although there is more in the file)
			in.close();
		}
	}
}
