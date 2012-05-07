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
