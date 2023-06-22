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
package org.zephyrsoft.sdb2.model.calendar;

import java.time.ZonedDateTime;

/**
 * An entry of a calendar. Simple and flattened, any recurrence was already applied.
 */
public class SimpleEvent {
	private final ZonedDateTime start;
	private final ZonedDateTime end;
	private final String title;
	private final String description;
	private final String location;
	
	public SimpleEvent(ZonedDateTime start, ZonedDateTime end, String title, String description, String location) {
		this.start = start;
		this.end = end;
		this.title = title;
		this.description = description;
		this.location = location;
	}
	
	public ZonedDateTime getStart() {
		return start;
	}
	
	public ZonedDateTime getEnd() {
		return end;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getLocation() {
		return location;
	}
	
	public boolean isMultiDay() {
		return !start.toLocalDate().isEqual(end.toLocalDate());
	}
	
	@Override
	public String toString() {
		return "SimpleEvent [start=" + start + ", end=" + end + ", title=" + title + ", description=" + description + ", location=" + location + "]";
	}
}
