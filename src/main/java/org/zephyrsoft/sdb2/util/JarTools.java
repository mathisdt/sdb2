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
package org.zephyrsoft.sdb2.util;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Helper class for using features of JARs, e.g. reading from the manifest.
 */
public final class JarTools {
	
	private JarTools() {
		// this class is not intended for instantiation
	}
	
	public static String getAttributeFromManifest(Class<?> classToUse, String attributeName) {
		String ret = null;
		InputStream manifestStream = ResourceTools.getInputStream(classToUse, "/META-INF/MANIFEST.MF");
		try {
			Manifest manifest = new Manifest(manifestStream);
			Attributes attributes = manifest.getMainAttributes();
			ret = attributes.getValue(attributeName);
		} catch (Exception e) {
			// swallow exception here, just return null
		}
		
		return ret;
	}
	
}
