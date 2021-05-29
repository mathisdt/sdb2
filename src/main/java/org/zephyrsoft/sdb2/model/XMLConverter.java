/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.model;

import java.io.InputStream;
import java.io.OutputStream;

import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Converts {@link Persistable} models to and from XML.
 */
public class XMLConverter {
	
	public static void fromPersistableToXML(Persistable model, OutputStream outputStream) {
		try {
			Marshaller marshaller = createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(model, outputStream);
		} catch (JAXBException e) {
			throw new IllegalStateException("could not marshal model to XML", e);
		}
	}
	
	public static <T extends Persistable> T fromXMLToPersistable(InputStream xmlInputStream) {
		try {
			Unmarshaller unmarshaller = createUnmarshaller();
			@SuppressWarnings("unchecked")
			T model = (T) unmarshaller.unmarshal(xmlInputStream);
			model.initIfNecessary();
			return model;
		} catch (JAXBException e) {
			throw new IllegalStateException("could not unmarshal model from XML", e);
		}
	}
	
	private static JAXBContext createContext() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(SongsModel.class, SettingsModel.class, StatisticsModel.class);
		return context;
	}
	
	private static Marshaller createMarshaller() throws JAXBException {
		JAXBContext context = createContext();
		return context.createMarshaller();
	}
	
	private static Unmarshaller createUnmarshaller() throws JAXBException {
		JAXBContext context = createContext();
		return context.createUnmarshaller();
	}
	
}
