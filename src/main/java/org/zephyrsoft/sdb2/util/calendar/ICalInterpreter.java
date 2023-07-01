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

import java.io.IOException;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

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
	private final ZonedDateTime startOfDisplayPeriod;
	private final int lengthOfDisplayPeriodInDays;
	private final HttpClient client;
	
	public ICalInterpreter(String url, ZonedDateTime startOfDisplayPeriod, int lengthOfDisplayPeriodInDays, Locale locale) {
		this.url = url;
		this.startOfDisplayPeriod = startOfDisplayPeriod;
		this.lengthOfDisplayPeriodInDays = lengthOfDisplayPeriodInDays;
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
			icalString = fetchCalendarData();
			
			StringReader reader = new StringReader(icalString);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(reader);
			
			LocalDateTime start = startOfDisplayPeriod.toLocalDateTime();
			LocalDateTime end = start.plusDays(lengthOfDisplayPeriodInDays).with(LocalTime.MAX);
			DateTime from = new DateTime(start.format(DEFAULT_FORMATTER));
			DateTime to = new DateTime(end.format(DEFAULT_FORMATTER));
			Period period = new Period(from, to);
			
			List<SimpleEvent> simpleEvents = calendar.getComponents("VEVENT").stream()
				.flatMap(component -> {
					PeriodList list = component.calculateRecurrenceSet(period);
					
					return list.stream()
						.map(p -> {
							return new SimpleEvent(convert(p.getStart(), true), convert(p.getEnd(), false),
								extractSummary(p), extractDescription(p), extractLocation(p));
						})
						.flatMap(this::splitMultiDayEvents);
				})
				.filter(se -> se.getStart().isAfter(startOfDisplayPeriod))
				.sorted(Comparator.comparing(SimpleEvent::getStart).thenComparing(SimpleEvent::getTitle).thenComparing(SimpleEvent::getDescription))
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
	
	String fetchCalendarData() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		return response.body();
	}
	
	private ZonedDateTime convert(DateTime dt, boolean isStart) {
		if (dt.getTimeZone() != null) {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), dt.getTimeZone().toZoneId());
		} else {
			ZonedDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), ZoneId.of("Z"))
				.toLocalDate()
				.atStartOfDay()
				.atZone(ZoneId.of("Z"));
			
			if (isStart) {
				return dateTime;
			} else {
				return dateTime.minusSeconds(1);
			}
		}
	}
	
	private Stream<SimpleEvent> splitMultiDayEvents(SimpleEvent e) {
		if (!e.isMultiDay()) {
			return Stream.of(e);
		} else {
			List<SimpleEvent> result = new ArrayList<>();
			
			LocalDate day = e.getStart().toLocalDate();
			while (!day.isAfter(e.getEnd().toLocalDate())) {
				ZonedDateTime startTime = day.equals(e.getStart().toLocalDate())
					? e.getStart()
					: ZonedDateTime.of(day, LocalTime.MIN, e.getStart().getZone());
				ZonedDateTime endTime = day.equals(e.getEnd().toLocalDate())
					? e.getEnd()
					: ZonedDateTime.of(day, LocalTime.MAX, e.getEnd().getZone());
				
				result.add(new SimpleEvent(startTime, endTime, e.getTitle(), e.getDescription(), e.getLocation()));
				
				day = day.plusDays(1);
			}
			
			return result.stream();
		}
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
