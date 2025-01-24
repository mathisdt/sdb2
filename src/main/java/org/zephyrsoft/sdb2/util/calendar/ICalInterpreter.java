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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.calendar.SimpleEvent;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;

/**
 * Reads iCal data from an URL and interprets it.
 */
public class ICalInterpreter {
	
	private final DateTimeFormatter displayDateFormatter;
	private final DateTimeFormatter displayTimeFormatter;
	
	private final String url;
	private final OffsetDateTime startOfDisplayPeriod;
	private final int lengthOfDisplayPeriodInDays;
	private final HttpClient client;

	static {
		System.setProperty("ical4j.parsing.relaxed", "true");
	}
	
	public ICalInterpreter(String url, OffsetDateTime startOfDisplayPeriod, int lengthOfDisplayPeriodInDays, Locale locale) {
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
		try {
			String icalString = fetchCalendarData();
			
			StringReader reader = new StringReader(icalString);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(reader);

			OffsetDateTime start = startOfDisplayPeriod;
			OffsetDateTime end = start.plusDays(lengthOfDisplayPeriodInDays).with(LocalTime.MAX);
			Period<OffsetDateTime> period = new Period<>(start, end);
			
			List<SimpleEvent> simpleEvents = calendar.getComponents("VEVENT").stream()
				.flatMap(component -> {
					Set<Period<Temporal>> list = component.calculateRecurrenceSet(period);
					
					return list.stream()
						.map(p -> new SimpleEvent(convert(p, true), convert(p, false),
                            extractSummary(p), extractDescription(p), extractLocation(p)))
						.flatMap(ICalInterpreter::splitMultiDayEvents);
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
	
	private static OffsetDateTime convert(Period<Temporal> period, boolean useStart) {
        if (useStart) {
			return ensureOffsetDateTime(period.getStart());
		} else {
			OffsetDateTime endMinusOneSec = ensureOffsetDateTime(period.getEnd()).minusSeconds(1);
			return endMinusOneSec.isAfter(ensureOffsetDateTime(period.getStart()))
				? endMinusOneSec
				: ensureOffsetDateTime(period.getEnd());
		}
	}

	private static OffsetDateTime ensureOffsetDateTime(Temporal temporal) {
		return switch (temporal) {
			case OffsetDateTime odt -> odt;
			case ZonedDateTime zdt -> zdt.toOffsetDateTime();
			case LocalDate ld -> OffsetDateTime.of(ld, LocalTime.MIN, systemDefaultOffset(LocalDateTime.of(ld, LocalTime.MIN)));
			case LocalDateTime ldt -> OffsetDateTime.of(ldt, systemDefaultOffset(ldt));
			case null, default -> throw new UnsupportedOperationException("could not convert "
				+ Objects.requireNonNull(temporal).getClass() + " to an OffsetDateTime");
		};
	}

	private static ZoneOffset systemDefaultOffset(LocalDateTime timestamp) {
		ZoneId zoneId = ZoneId.systemDefault();
		ZoneRules rules = zoneId.getRules();
		return rules.getOffset(timestamp);
	}
	
	private static Stream<SimpleEvent> splitMultiDayEvents(SimpleEvent e) {
		if (!e.isMultiDay()) {
			return Stream.of(e);
		} else {
			List<SimpleEvent> result = new ArrayList<>();
			
			LocalDate day = e.getStart().toLocalDate();
			while (!day.isAfter(e.getEnd().toLocalDate())) {
				OffsetDateTime startTime = day.equals(e.getStart().toLocalDate())
					? e.getStart()
					: OffsetDateTime.of(day, LocalTime.MIN, e.getStart().getOffset());
				OffsetDateTime endTime = day.equals(e.getEnd().toLocalDate())
					? e.getEnd()
					: OffsetDateTime.of(day, LocalTime.MAX, e.getEnd().getOffset());
				
				result.add(new SimpleEvent(startTime, endTime, e.getTitle(), e.getDescription(), e.getLocation()));
				
				day = day.plusDays(1);
			}
			
			return result.stream();
		}
	}
	
	private static String extractSummary(Period<Temporal> veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getSummary() != null) {
			return vevent.getSummary().getValue();
		}
		return null;
	}
	
	private static String extractDescription(Period<Temporal> veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getDescription() != null) {
			return vevent.getDescription().getValue();
		}
		return null;
	}
	
	private static String extractLocation(Period<Temporal> veventPeriod) {
		if (veventPeriod != null
			&& veventPeriod.getComponent() instanceof VEvent vevent
			&& vevent.getLocation() != null) {
			return vevent.getLocation().getValue();
		}
		return null;
	}
	
}
