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

import java.awt.Font;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A XStream converter for AWT fonts.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class FontConverter implements Converter {
	
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Font.class);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		Font font = (Font) value;
		writer.startNode("name");
		writer.setValue(font.getFontName());
		writer.endNode();
		writer.startNode("style");
		writer.setValue(String.valueOf(font.getStyle()));
		writer.endNode();
		writer.startNode("size");
		writer.setValue(String.valueOf(font.getSize()));
		writer.endNode();
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String name = null;
		int style = 0;
		int size = 0;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("name".equals(reader.getNodeName())) {
				name = reader.getValue();
			} else if ("style".equals(reader.getNodeName())) {
				style = Integer.parseInt(reader.getValue());
			} else if ("size".equals(reader.getNodeName())) {
				size = Integer.parseInt(reader.getValue());
			}
			reader.moveUp();
		}
		return new Font(name, style, size);
	}
	
}
