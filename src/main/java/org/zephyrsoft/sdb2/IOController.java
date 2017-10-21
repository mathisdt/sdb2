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
package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for input/output operations.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class IOController {
	
	private static final Logger LOG = LoggerFactory.getLogger(IOController.class);
	
	public <T> T readSongs(String fileName, Function<InputStream, T> handler) {
		String fileNameToUse = FileAndDirectoryLocations.getSongsFileName(fileName);
		File file = new File(fileNameToUse);
		LOG.debug("reading songs from {}", file.getAbsolutePath());
		T result = null;
		try (InputStream xmlInputStream = new FileInputStream(file)) {
			result = handler.apply(xmlInputStream);
		} catch (IOException e) {
			LOG.error("could not read songs from \"" + file.getAbsolutePath() + "\"", e);
		}
		return result;
	}
	
	public <T> T readSettings(Function<InputStream, T> handler) {
		File file = new File(FileAndDirectoryLocations.getSettingsFileName());
		LOG.debug("reading settings from {}", file.getAbsolutePath());
		T result = null;
		try (InputStream xmlInputStream = new FileInputStream(file)) {
			result = handler.apply(xmlInputStream);
		} catch (IOException e) {
			LOG.error("could not read settings from \"" + file.getAbsolutePath() + "\"", e);
		}
		return result;
	}
	
	public <T> T readStatistics(Function<InputStream, T> handler) {
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		LOG.debug("reading statistics from {}", file.getAbsolutePath());
		T result = null;
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			result = handler.apply(xmlInputStream);
			xmlInputStream.close();
		} catch (IOException e) {
			LOG.error("could not read statistics from \"" + file.getAbsolutePath() + "\"");
		}
		return result;
	}
	
}
