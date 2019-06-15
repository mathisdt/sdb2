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
package org.zephyrsoft.sdb2.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.util.SongsModelListener;
import org.zephyrsoft.sdb2.util.gui.TransparentFilterableListModel;
import org.zephyrsoft.sdb2.util.gui.TransparentListModel;

/**
 * Model for {@link MainWindow}.
 */
@XmlRootElement(name = "songs")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class SongsModel implements Iterable<Song>, Persistable {
	
	private static final Logger LOG = LoggerFactory.getLogger(SongsModel.class);
	
	@XmlElement(name = "autoSort")
	private boolean autoSort = true;
	
	@XmlElement(name = "song")
	private List<Song> songs = null;
	
	private List<TransparentListModel<Song>> createdListModels = null;
	
	private List<SongsModelListener> songsModelListeners = null;
	
	public SongsModel() {
		initIfNecessary();
	}
	
	/**
	 * Should this model instance sort the songs automatically? Default: {@code true}
	 *
	 * @param value
	 *            the value to set
	 */
	public void setAutoSort(boolean value) {
		this.autoSort = value;
	}
	
	public boolean isAutoSort() {
		return autoSort;
	}
	
	@Override
	public final void initIfNecessary() {
		if (songs == null) {
			songs = new ArrayList<>();
		}
		if (autoSort) {
			sortSongs();
		}
		if (createdListModels == null) {
			createdListModels = new ArrayList<>();
		}
		if (songsModelListeners == null) {
			songsModelListeners = new ArrayList<>();
		}
	}
	
	public TransparentListModel<Song> getListModel() {
		TransparentListModel<Song> createdListModel = new TransparentListModel<>(songs);
		createdListModels.add(createdListModel);
		return createdListModel;
	}
	
	public TransparentFilterableListModel<Song> getFilterableListModel() {
		TransparentFilterableListModel<Song> createdListModel = new TransparentFilterableListModel<>(songs);
		createdListModels.add(createdListModel);
		return createdListModel;
	}
	
	public int getSize() {
		return songs.size();
	}
	
	public boolean isEmpty() {
		return songs.isEmpty();
	}
	
	public boolean addSong(Song e) {
		boolean b = songs.add(e);
		if (autoSort) {
			sortSongs();
		}
		notifyListModelListeners();
		return b;
	}
	
	public void insertSong(int index, Song e) {
		if (autoSort) {
			throw new IllegalStateException("should insert song at a specific position, but auto-sorting is enabled");
		} else {
			songs.add(index, e);
			notifyListModelListeners();
		}
	}
	
	public boolean removeSong(Song o) {
		boolean b = songs.remove(o);
		if (autoSort) {
			sortSongs();
		}
		notifyListModelListeners();
		return b;
	}
	
	public Song removeSong(int index) {
		Song ret = songs.remove(index);
		if (autoSort) {
			sortSongs();
		}
		notifyListModelListeners();
		return ret;
	}
	
	private void notifyListModelListeners() {
		LOG.debug("notifyListModelListeners");
		for (TransparentListModel<Song> model : createdListModels) {
			ListDataListener[] listeners = model.getListDataListeners();
			for (ListDataListener listener : listeners) {
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, songs.size() - 1));
			}
		}
		for (SongsModelListener listener : songsModelListeners) {
			listener.songsModelChanged();
		}
	}
	
	public Song getSong(int index) {
		return songs.get(index);
	}
	
	public Song getByUUID(String uuid) {
		for (Song song : songs) {
			if (song.getUUID() != null && song.getUUID().equals(uuid)) {
				return song;
			}
		}
		return null;
	}
	
	@Override
	public Iterator<Song> iterator() {
		return songs.iterator();
	}
	
	private void sortSongs() {
		Collections.sort(songs);
	}
	
	public void sortAndUpdateView() {
		if (autoSort) {
			sortSongs();
		}
		notifyListModelListeners();
	}
	
	public Collection<Song> getSongs() {
		return Collections.unmodifiableList(songs);
	}
	
	public void addSongsModelListener(SongsModelListener songsModelListener) {
		songsModelListeners.add(songsModelListener);
	}
	
}
