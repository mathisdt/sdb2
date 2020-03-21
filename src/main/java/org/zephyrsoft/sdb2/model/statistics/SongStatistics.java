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
package org.zephyrsoft.sdb2.model.statistics;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.util.converter.LocalDateAdapter;

import com.google.common.base.Preconditions;

/**
 * Statistics about the displaying of one specific song.
 */
@XmlRootElement(name = "songStatistics")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class SongStatistics implements Comparable<SongStatistics>, Iterable<LocalDate> {
	
	@XmlElement(name = "songUuid")
	private String songUuid;
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
	 *
	 * @param songUuid
	 *            the UUID which belongs to the song that these statistics are kept for
	 */
	public SongStatistics(String songUuid) {
		Preconditions.checkArgument(songUuid != null, "the UUID must be different from null");
		this.songUuid = songUuid;
	}
	
	public String getSongUUID() {
		return songUuid;
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
