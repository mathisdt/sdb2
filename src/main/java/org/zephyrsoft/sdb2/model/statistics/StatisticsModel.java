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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.XMLConverter;

/**
 * Statistics about the displaying of songs.
 * 
 * @author Mathis Dirksen-Thedens
 */
@XStreamAlias("statistics")
public class StatisticsModel {
	
	private static final Logger LOG = LoggerFactory.getLogger(StatisticsModel.class);
	
	@XStreamImplicit(itemFieldName = "songStatistics")
	private List<SongStatistics> songStatistics = null;
	
	public StatisticsModel() {
		initIfNecessary();
	}
	
	/**
	 * Is called from the local constructor and from {@link XMLConverter} to ensure a valid inner state after conversion
	 * from XML and after creation via constructor. This is in this method because XStream might overwrite the value set
	 * inside the constructor with {@code null}.
	 */
	public final void initIfNecessary() {
		if (songStatistics == null) {
			songStatistics = new ArrayList<>();
		}
	}
	
	public void addStatisticsEntry(Song song, Date date) {
		SongStatistics stats = getStatistics(song);
		if (stats == null) {
			LOG.debug("creating SongStatistics for {} / UUID={}", song.getTitle(), song.getUUID());
			stats = new SongStatistics(song.getUUID());
			songStatistics.add(stats);
		}
		LOG.debug("adding date for {} / UUID={}", song.getTitle(), song.getUUID());
		stats.dateAdd(date);
		
	}
	
	public SongStatistics getStatistics(Song song) {
		return getStatistics(song.getUUID());
	}
	
	public SongStatistics getStatistics(String songUuid) {
		Validate.notNull(songUuid, "UUID must be different from null");
		SongStatistics ret = null;
		for (SongStatistics stats : songStatistics) {
			if (songUuid.equals(stats.getSongUUID())) {
				ret = stats;
				break;
			}
		}
		return ret;
	}
	
	private static SimpleDateFormat createYearMonthFormatter() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		return sdf;
	}
	
	public List<String> getUsedMonths() {
		SortedSet<String> ret = new TreeSet<>();

		SimpleDateFormat sdf = createYearMonthFormatter();
		for (SongStatistics stat : songStatistics) {
			for (Date date : stat)
				ret.add(sdf.format(date));
		}

		return new ArrayList<>(ret);
	}
	
	public Map<String, Integer> getStatisticsForMonth(String yearAndMonth) {
		if (yearAndMonth == null) {
			return null;
		}
		Map<String, Integer> ret = new HashMap<>();
		SimpleDateFormat sdf = createYearMonthFormatter();
		for (SongStatistics stat : songStatistics) {
			for (Date date : stat) {
				if (yearAndMonth.equals(sdf.format(date))) {
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
