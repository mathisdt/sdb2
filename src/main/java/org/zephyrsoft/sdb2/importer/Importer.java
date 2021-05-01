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
package org.zephyrsoft.sdb2.importer;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.zephyrsoft.sdb2.model.Song;

/**
 * Importer for foreign formats.
 */
public interface Importer {
	
	/**
	 * Get the name of the source program from which the import comes.
	 */
	String getSourceName();
	
	/**
	 * Return a scheme describing which files are accepted by the {@link #getFileFilter()} method.
	 */
	String getFileScheme();
	
	/**
	 * Return a file filter to just display the files which are permitted to import.
	 */
	FileFilter getFileFilter();
	
	/**
	 * Read the indicated file and transform the songs found therein to the native SDBv2 format.
	 *
	 * @param inputFile
	 *            the file to read (may not be {@code null})
	 * @return all songs found in the file
	 */
	List<Song> loadFromFile(File inputFile);
	
}
