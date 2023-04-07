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
package org.zephyrsoft.sdb2.util.calendar;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.calendar.SimpleEvent;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Reads iCal data from an URL and interprets it.
 */
public class ICalInterpreter {
	
	private static final String DEFAULT_PATTERN = "yyyyMMdd'T'HHmmss";
	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
	private final DateTimeFormatter displayDateFormatter;
	private final DateTimeFormatter displayTimeFormatter;
	
	private final String url;
	private final int daysAhead;
	private final HttpClient client;
	
	public ICalInterpreter(String url, int daysAhead, Locale locale) {
		this.url = url;
		this.daysAhead = daysAhead;
		client = HttpClient.newBuilder()
			.followRedirects(Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(15))
			.build();
		displayDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
		displayTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale);
	}
	
	public Song getInterpretedData() {
		String icalString = null;
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			icalString = response.body();
			
			StringReader reader = new StringReader(icalString);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(reader);
			
			ZonedDateTime zonedStart = ZonedDateTime.now();
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = start.plusDays(daysAhead).with(LocalTime.MAX);
			DateTime from = new DateTime(start.format(DEFAULT_FORMATTER));
			DateTime to = new DateTime(end.format(DEFAULT_FORMATTER));
			Period period = new Period(from, to);
			
			List<SimpleEvent> simpleEvents = calendar.getComponents("VEVENT").stream()
				.flatMap(component -> {
					PeriodList list = component.calculateRecurrenceSet(period);
					
					return list.stream()
						.map(p -> new SimpleEvent(convert(p.getStart()), convert(p.getEnd()),
							extractSummary(p), extractDescription(p), extractLocation(p)));
				})
				.filter(se -> se.getStart().isAfter(zonedStart))
				.sorted(Comparator.comparing(SimpleEvent::getStart).thenComparing(SimpleEvent::getTitle))
				.toList();
			
			StringBuilder calendarText = new StringBuilder();
			LocalDate alreadyAddedDate = null;
			for (SimpleEvent se : simpleEvents) {
				if (alreadyAddedDate == null) {
					alreadyAddedDate = se.getStart().toLocalDate();
					calendarText.append(se.getStart().format(displayDateFormatter)).append("\n");
				} else if (se.getStart().toLocalDate().isAfter(alreadyAddedDate)) {
					alreadyAddedDate = se.getStart().toLocalDate();
					calendarText.append("\n").append(se.getStart().format(displayDateFormatter)).append("\n");
				}
				calendarText
					.append("   ")
					.append(se.getStart().format(displayTimeFormatter))
					.append("  ")
					.append(se.getTitle())
					.append("\n");
			}
			Song calendarSong = new Song(UUID.randomUUID().toString());
			calendarSong.setLyrics(calendarText.toString());
			return calendarSong;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private ZonedDateTime convert(DateTime dt) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), dt.getTimeZone().toZoneId());
	}
	
	private String extractSummary(Period veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getSummary() != null) {
			return vevent.getSummary().getValue();
		}
		return null;
	}
	
	private String extractDescription(Period veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getDescription() != null) {
			return vevent.getDescription().getValue();
		}
		return null;
	}
	
	private String extractLocation(Period veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getLocation() != null) {
			return vevent.getLocation().getValue();
		}
		return null;
	}
	
}
