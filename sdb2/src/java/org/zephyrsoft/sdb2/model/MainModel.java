package org.zephyrsoft.sdb2.model;

import java.io.*;
import java.util.*;
import org.zephyrsoft.sdb2.gui.*;

/**
 * Model for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainModel implements Iterable<Song>, Serializable {
	
	private static final long serialVersionUID = -2503516988752281994L;
	
	private List<Song> songs = null;
	
	/**
	 * Is called from {@link XMLConverter} to ensure a valid inner state after conversion from XML.
	 */
	public void initIfNecessary() {
		if (songs == null) {
			songs = new ArrayList<Song>();
		}
	}
	
	public List<Song> getAllSongs() {
		return Collections.unmodifiableList(songs);
	}
	
	public int getSize() {
		return songs.size();
	}
	
	public boolean isEmpty() {
		return songs.isEmpty();
	}
	
	public boolean addSong(Song e) {
		return songs.add(e);
	}
	
	public boolean removeSong(Song o) {
		return songs.remove(o);
	}
	
	public Song getSong(int index) {
		return songs.get(index);
	}
	
	@Override
	public Iterator<Song> iterator() {
		return songs.iterator();
	}
	
}
