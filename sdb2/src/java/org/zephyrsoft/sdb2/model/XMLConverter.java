package org.zephyrsoft.sdb2.model;

import java.io.InputStream;
import java.io.OutputStream;
import com.thoughtworks.xstream.XStream;
import org.zephyrsoft.sdb2.model.converter.LanguageEnumConverter;

/**
 * Converts the {@link SongsModel} to and from XML.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class XMLConverter {
	
	public static void fromSongsModelToXML(SongsModel model, OutputStream outputStream) {
		XStream xstream = initXStream();
		xstream.toXML(model, outputStream);
	}
	
	public static SongsModel fromXMLToSongsModel(InputStream xmlInputStream) {
		XStream xstream = initXStream();
		SongsModel model = (SongsModel) xstream.fromXML(xmlInputStream);
		model.initIfNecessary();
		return model;
	}
	
	public static void fromSettingsModelToXML(SettingsModel model, OutputStream outputStream) {
		XStream xstream = initXStream();
		xstream.toXML(model, outputStream);
	}
	
	public static SettingsModel fromXMLToSettingsModel(InputStream xmlInputStream) {
		XStream xstream = initXStream();
		SettingsModel model = (SettingsModel) xstream.fromXML(xmlInputStream);
		model.initIfNecessary();
		return model;
	}
	
	private static XStream initXStream() {
		XStream xstream = new XStream();
		// aliases and omitted fields of model classes are defined via annotations
		xstream.processAnnotations(SettingsModel.class);
		xstream.processAnnotations(SongsModel.class);
		xstream.processAnnotations(Song.class);
		// custom converter for LanguageEnum
		xstream.registerConverter(new LanguageEnumConverter());
		return xstream;
	}
	
}
