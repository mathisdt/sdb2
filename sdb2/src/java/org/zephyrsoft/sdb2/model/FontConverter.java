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
