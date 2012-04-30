package org.zephyrsoft.sdb2.importer;

import java.io.File;
import java.util.List;
import org.zephyrsoft.sdb2.model.Song;

/**
 * Importer for foreign formats.
 * 
 * @author Mathis Dirksen-Thedens
 */
public interface Importer {
	
	/**
	 * Read the indicated file and transform the songs found therein to the native SDB format.
	 * 
	 * @param inputFile the file to read (may not be {@code null})
	 * @return all songs found in the file
	 */
	List<Song> loadFromFile(File inputFile);
	
}
