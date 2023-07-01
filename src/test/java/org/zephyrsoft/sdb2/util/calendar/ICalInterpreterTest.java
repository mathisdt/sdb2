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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class ICalInterpreterTest {
	
	@Test
	public void wholeDayEvents() {
		ICalInterpreter iCalInterpreter = new ICalInterpreter(
			null,
			ZonedDateTime.of(2023, 6, 22, 23, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			7,
			Locale.GERMAN) {
			@Override
			String fetchCalendarData() throws IOException, InterruptedException {
				// the test data contains both:
				// a single-day event without time (which means that it's valid for the whole day) = "Street-Church"
				// a multi-day event without time (which means that it's valid for all of the days) = "Männerfreizeit"
				return readResourceAsString("/multiple-days-event-without-time.ics");
			}
		};
		String interpreted = iCalInterpreter.getInterpretedData().getLyrics();
		
		assertEquals(readResourceAsString("/multiple-days-event-without-time.txt"), interpreted);
	}
	
	private static String readResourceAsString(String resourceName) {
		try {
			return Files.readString(Paths.get(ICalInterpreterTest.class.getResource(resourceName).toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
}
