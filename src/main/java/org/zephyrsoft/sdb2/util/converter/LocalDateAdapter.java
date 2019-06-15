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
package org.zephyrsoft.sdb2.util.converter;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.zephyrsoft.sdb2.util.DateTools;

/**
 * XML adapter for {@link LocalDate}.
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
	
	@Override
	public LocalDate unmarshal(String v) throws Exception {
		return DateTools.parseDate(v);
	}
	
	@Override
	public String marshal(LocalDate v) throws Exception {
		return DateTools.formatDate(v);
	}
}
