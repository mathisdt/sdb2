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
package org.zephyrsoft.sdb2.util;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.zephyrsoft.util.DateTools;

public class DateToolsTest {
	
	@Test
	public void parseDateTime() {
		LocalDateTime dateTime = DateTools.parseDateTime("2016-10-09T20:24:25+02:00[Europe/Berlin]");
		Assert.assertEquals(LocalDateTime.of(2016, 10, 9, 20, 24, 25), dateTime);
	}
	
}
