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
package org.zephyrsoft.sdb2.model.statistics;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.util.converter.LocalDateAdapter;

import com.google.common.base.Preconditions;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Statistics about the displaying of one specific song.
 */
@XmlRootElement(name = "songStatistics")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class SongStatistics implements Comparable<SongStatistics>, Iterable<LocalDate> {
	
	@XmlElement(name = "songUuid")
	private String songUuid;
	@XmlElement(name = "songTitle", required = false)
	private String songTitle;
	@XmlElementWrapper(name = "presentedOn")
	@XmlElement(name = "date")
	@XmlJavaTypeAdapter(LocalDateAdapter.class)
	private SortedSet<LocalDate> presentedOn = new TreeSet<>();
	
	/**
	 * CAUTION: every statistics element has to have a song UUID! This constructor is only necessary for
	 * unmarshalling from XML.
	 */
	public SongStatistics() {
		// default constructor
	}
	
	/**
	 * Create a statistics element for a single {@link Song}.
	 */
	public SongStatistics(Song song) {
		Preconditions.checkArgument(song.getUUID() != null, "the UUID must be different from null");
		this.songUuid = song.getUUID();
		this.songTitle = song.getTitle();
	}
	
	public String getSongUUID() {
		return songUuid;
	}
	
	public String getSongTitle() {
		return songTitle;
	}
	
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}
	
	@Override
	public Iterator<LocalDate> iterator() {
		return presentedOn.iterator();
	}
	
	public boolean dateAdd(LocalDate date) {
		return presentedOn.add(date);
	}
	
	@Override
	public int compareTo(SongStatistics o) {
		if (o == null || o.getSongUUID() == null) {
			return 1;
		} else if (getSongUUID() == null) {
			return -1;
		} else {
			return getSongUUID().compareTo(o.getSongUUID());
		}
	}
	
}
