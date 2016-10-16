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
package org.zephyrsoft.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Helps with handling dates.
 */
public class DateTools {
	
	private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
	
	public static LocalDateTime parseDateTime(String toParse) {
		if (toParse != null && toParse.length() > 0) {
			return ZonedDateTime.parse(toParse).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			return null;
		}
	}
	
	public static LocalDateTime toLocalDateTime(Date date) {
		if (date != null) {
			return ZonedDateTime.from(date.toInstant()).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			return null;
		}
	}
	
	/**
	 * checks if the first parameter is before <b>the second plus 15 minutes</b>
	 */
	public static boolean isMax15MinutesLater(LocalDateTime one, LocalDateTime two) {
		return one.isBefore(two.plusMinutes(15));
	}
	
	public static String format(LocalDateTime latestReleaseTimestamp) {
		return latestReleaseTimestamp.format(DATE_TIME_FORMATTER);
	}
}
