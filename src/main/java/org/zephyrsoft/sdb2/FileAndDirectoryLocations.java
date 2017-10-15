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

import org.zephyrsoft.util.DateTools;

/**
 * Manages (default) locations of directories and files.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class FileAndDirectoryLocations {
	
	public static final String BASE_DIR_STRING = System.getProperty("user.home") + File.separator + ".songdatabase";
	public static final String SONGS_SUBDIR_STRING = "songs";
	public static final String BACKUP_SUBDIR_STRING = "backup";
	public static final String SONGS_FILE_STRING = "songs.xml";
	public static final String SONGS_BACKUP_FILE_STRING = "songs-%s.xml";
	public static final String SETTINGS_SUBDIR_STRING = "settings";
	public static final String SETTINGS_FILE_STRING = "settings.xml";
	public static final String STATISTICS_SUBDIR_STRING = "statistics";
	public static final String STATISTICS_FILE_STRING = "statistics.xml";
	public static final String LOG_SUBDIR_STRING = "log";
	
	public static String getDefaultSongsFileName() {
		return getSongsDir() + File.separator + SONGS_FILE_STRING;
	}
	
	public static String getSettingsFileName() {
		return getSettingsDir() + File.separator + SETTINGS_FILE_STRING;
	}
	
	public static String getStatisticsFileName() {
		return getStatisticsDir() + File.separator + STATISTICS_FILE_STRING;
	}
	
	private static String getSongsDir() {
		return getDir(SONGS_SUBDIR_STRING);
	}
	
	public static String getSongsBackupDir() {
		return getDir(SONGS_SUBDIR_STRING + File.separator + BACKUP_SUBDIR_STRING);
	}
	
	public static String getSongsBackupFile() {
		return getDir(SONGS_SUBDIR_STRING + File.separator + BACKUP_SUBDIR_STRING)
			+ File.separator + String.format(SONGS_BACKUP_FILE_STRING, DateTools.timestamp());
	}
	
	private static String getSettingsDir() {
		return getDir(SETTINGS_SUBDIR_STRING);
	}
	
	private static String getStatisticsDir() {
		return getDir(STATISTICS_SUBDIR_STRING);
	}
	
	/**
	 * This method is only for telling the user where the log file resides! Configuration of log writing is subject to
	 * the log framework's config files!
	 */
	public static String getLogDir() {
		return getDir(LOG_SUBDIR_STRING);
	}
	
	private static String getDir(String subDirectory) {
		String path = BASE_DIR_STRING + File.separator + subDirectory;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return path;
	}
}
