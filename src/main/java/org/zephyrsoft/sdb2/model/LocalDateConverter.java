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

import java.time.LocalDate;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A custom XStream converter for {@link LocalDate} objects.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class LocalDateConverter implements Converter {
	
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(LocalDate.class);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		LocalDate date = (LocalDate) value;
		writer.startNode("year");
		writer.setValue(String.valueOf(date.getYear()));
		writer.endNode();
		writer.startNode("month");
		writer.setValue(String.valueOf(date.getMonthValue()));
		writer.endNode();
		writer.startNode("day");
		writer.setValue(String.valueOf(date.getDayOfMonth()));
		writer.endNode();
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		int year = 0;
		int month = 0;
		int dayOfMonth = 0;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("year".equals(reader.getNodeName())) {
				year = Integer.parseInt(reader.getValue());
			} else if ("month".equals(reader.getNodeName())) {
				month = Integer.parseInt(reader.getValue());
			} else if ("day".equals(reader.getNodeName())) {
				dayOfMonth = Integer.parseInt(reader.getValue());
			}
			reader.moveUp();
		}
		return LocalDate.of(year, month, dayOfMonth);
	}
	
}
