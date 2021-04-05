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
package org.zephyrsoft.sdb2.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Provides utility methods for fetching resources.
 */
public final class ResourceTools {
	
	private ResourceTools() {
		// no instantiation allowed
	}
	
	/**
	 * Load an input stream from a resource.
	 */
	public static InputStream getInputStream(Class<?> classToUse, String resourcePath) {
		String path = cleanPath(resourcePath);
		InputStream ret = classToUse.getResourceAsStream(path);
		
		if (ret == null) {
			// try again using a "root" slash
			ret = classToUse.getResourceAsStream("/" + path);
		}
		return ret;
	}
	
	/**
	 * Load an icon from a resource.
	 */
	public static Icon getIcon(Class<?> classToUse, String resourcePath) {
		String path = cleanPath(resourcePath);
		URL url = classToUse.getResource(path);
		if (url == null) {
			// try again using a "root" slash
			url = classToUse.getResource("/" + path);
		}
		return new ImageIcon(url);
	}
	
	/**
	 * Load an image from a resource.
	 */
	public static Image getImage(Class<?> classToUse, String resourcePath) {
		String path = cleanPath(resourcePath);
		URL url = classToUse.getResource(path);
		if (url == null) {
			// try again using a "root" slash
			url = classToUse.getResource("/" + path);
		}
		return Toolkit.getDefaultToolkit().getImage(url);
	}
	
	private static String cleanPath(String pathToClean) {
		String path = pathToClean;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}
	
}
