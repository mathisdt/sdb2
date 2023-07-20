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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Persistable;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.util.DateTools;

import com.google.common.base.Preconditions;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Statistics about the displaying of songs.
 */
@XmlRootElement(name = "statistics")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class StatisticsModel implements Persistable {
	
	private static final Logger LOG = LoggerFactory.getLogger(StatisticsModel.class);
	
	@XmlElement(name = "songStatistics")
	private List<SongStatistics> songStatistics = null;
	
	public StatisticsModel() {
		initIfNecessary();
	}
	
	@Override
	public final void initIfNecessary() {
		if (songStatistics == null) {
			songStatistics = new ArrayList<>();
		}
	}
	
	public void addStatisticsEntry(Song song, LocalDate date) {
		SongStatistics stats = getStatistics(song);
		if (stats == null) {
			LOG.debug("creating SongStatistics for {} / UUID={}", song.getTitle(), song.getUUID());
			stats = new SongStatistics(song);
			songStatistics.add(stats);
		}
		LOG.debug("adding date for {} / UUID={}", song.getTitle(), song.getUUID());
		stats.dateAdd(date);
		
		// update title in case it was changed
		stats.setSongTitle(song.getTitle());
	}
	
	public SongStatistics getStatistics(Song song) {
		return getStatistics(song.getUUID());
	}
	
	public SongStatistics getStatistics(String songUuid) {
		Preconditions.checkArgument(songUuid != null, "UUID must be different from null");
		SongStatistics ret = null;
		for (SongStatistics stats : songStatistics) {
			if (songUuid.equals(stats.getSongUUID())) {
				ret = stats;
				break;
			}
		}
		return ret;
	}
	
	public List<String> getUsedMonths() {
		SortedSet<String> ret = new TreeSet<>();
		
		for (SongStatistics stat : songStatistics) {
			for (LocalDate date : stat)
				ret.add(DateTools.formatYearMonth(date));
		}
		
		return new ArrayList<>(ret);
	}
	
	public Map<String, Integer> getStatisticsForMonth(String yearAndMonth) {
		if (yearAndMonth == null) {
			return null;
		}
		Map<String, Integer> ret = new HashMap<>();
		for (SongStatistics stat : songStatistics) {
			for (LocalDate date : stat) {
				if (yearAndMonth.equals(DateTools.formatYearMonth(date))) {
					Integer upToNow = ret.get(stat.getSongUUID());
					if (upToNow == null) {
						upToNow = 1;
					} else {
						upToNow++;
					}
					ret.put(stat.getSongUUID(), upToNow);
				}
			}
		}
		return ret;
	}
	
}
