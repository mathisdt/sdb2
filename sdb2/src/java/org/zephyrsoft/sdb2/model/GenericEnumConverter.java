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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.zephyrsoft.util.StringTools;

/**
 * A XStream converter for arbitrary enum values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class GenericEnumConverter<E extends Enum<E>> implements Converter {
	
	private final Class<E> handledEnum;
	
	public GenericEnumConverter(Class<E> clazz) {
		this.handledEnum = clazz;
	}
	
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(handledEnum);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		// at this point, XStream did already make sure that the value parameter is of type E
		@SuppressWarnings("unchecked")
		E enumValue = (E) value;
		writer.setValue(StringTools.underscoredToCamelCase(enumValue.name()));
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		E enumValue = E.valueOf(handledEnum, StringTools.camelCaseToUnderscored(reader.getValue()));
		return enumValue;
	}
	
}
