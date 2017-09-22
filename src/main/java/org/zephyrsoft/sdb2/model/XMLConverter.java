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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.SongStatistics;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

/**
 * Converts models to and from XML.
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
	
	public static void fromStatisticsModelToXML(StatisticsModel model, OutputStream outputStream) {
		XStream xstream = initXStream();
		xstream.toXML(model, outputStream);
	}
	
	public static StatisticsModel fromXMLToStatisticsModel(InputStream xmlInputStream) {
		XStream xstream = initXStream();
		StatisticsModel model = (StatisticsModel) xstream.fromXML(xmlInputStream);
		model.initIfNecessary();
		return model;
	}
	
	private static XStream initXStream() {
		XStream xstream = new XStream();
		// clear out existing permissions and set own ones
		xstream.addPermission(NoTypePermission.NONE);
		// allow some classes
		xstream.addPermission(NullPermission.NULL);
		xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
		xstream.allowTypeHierarchy(Collection.class);
		xstream.allowTypesByWildcard(new String[] { "java.lang.**", "java.util.**", "java.awt.**", "java.time.**",
			"org.zephyrsoft.sdb2.**" });
		
		// aliases and omitted fields of model classes are defined via annotations
		xstream.processAnnotations(SongsModel.class);
		xstream.processAnnotations(Song.class);
		xstream.processAnnotations(SettingsModel.class);
		xstream.processAnnotations(StatisticsModel.class);
		xstream.processAnnotations(SongStatistics.class);
		
		// custom converters
		xstream.registerConverter(new GenericEnumConverter<>(FilterTypeEnum.class));
		xstream.registerConverter(new GenericEnumConverter<>(LanguageEnum.class));
		xstream.registerConverter(new GenericEnumConverter<>(ScreenContentsEnum.class));
		xstream.registerConverter(new FontConverter());
		xstream.registerConverter(new LocalDateConverter());
		
		// unknown XML elements can be ignored (e.g. <linkedSongs> which was removed)
		xstream.ignoreUnknownElements();
		
		return xstream;
	}
	
}
