package org.zephyrsoft.sdb2.model;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A XStream converter for {@link LanguageEnum} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class LanguageEnumConverter implements Converter {
	
	public static final String XSTREAM_ALIAS = "language";
	
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(LanguageEnum.class);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		LanguageEnum languageEnum = (LanguageEnum) value;
		writer.setValue(languageEnum.getLanguageAbbreviation());
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		LanguageEnum languageEnum = LanguageEnum.getByLanguageAbbreviation(reader.getValue());
		return languageEnum;
	}
	
}
