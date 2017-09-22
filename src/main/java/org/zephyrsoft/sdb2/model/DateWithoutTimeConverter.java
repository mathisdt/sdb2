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
package org.zephyrsoft.sdb2.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A XStream converter for {@link Date} objects which does only use the date and discards any time information.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class DateWithoutTimeConverter implements Converter {
	
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Date.class);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		Date date = (Date) value;
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		writer.startNode("year");
		writer.setValue(String.valueOf(cal.get(Calendar.YEAR)));
		writer.endNode();
		writer.startNode("month");
		writer.setValue(String.valueOf(cal.get(Calendar.MONTH)));
		writer.endNode();
		writer.startNode("day");
		writer.setValue(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
		writer.endNode();
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Calendar cal = new GregorianCalendar();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("year".equals(reader.getNodeName())) {
				cal.set(Calendar.YEAR, Integer.parseInt(reader.getValue()));
			} else if ("month".equals(reader.getNodeName())) {
				cal.set(Calendar.MONTH, Integer.parseInt(reader.getValue()));
			} else if ("day".equals(reader.getNodeName())) {
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(reader.getValue()));
			}
			reader.moveUp();
		}
		return cal.getTime();
	}
	
}
