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
package org.zephyrsoft.sdb2;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.zephyrsoft.sdb2.util.DateTools;

/**
 * Manages (default) locations of directories and files.
 */
public class FileAndDirectoryLocations {
	
	private static final String BASE_DIR_STRING = System.getProperty("user.home") + File.separator + ".songdatabase";
	
	private static final String SONGS_SUBDIR_STRING = "songs";
	private static final String SONGS_BACKUP_SUBDIR_STRING = "backup";
	private static final String SETTINGS_SUBDIR_STRING = "settings";
	private static final String STATISTICS_SUBDIR_STRING = "statistics";
	private static final String LOG_SUBDIR_STRING = "log";
	private static final String DB_SUBDIR_STRING = "db";
	
	private static final String SONGS_FILE_STRING = "songs.xml";
	private static final String SONGS_BACKUP_FILE_STRING = "songs-%s.xml";
	private static final String SETTINGS_FILE_STRING = "settings.xml";
	private static final String SETTINGS_FALLBACK_FILE_STRING = "settings-fallback.xml";
	private static final String STATISTICS_FILE_STRING = "statistics.xml";
	private static final String DB_FILE_STRING = "db.xml";
	private static final String DB_PROPERTIES_FILE_STRING = "db.properties.xml";
	
	public static String getDefaultSongsFileName() {
		return getSongsDir() + File.separator + SONGS_FILE_STRING;
	}
	
	/**
	 * If the parameter is not blank, return it - else return the default location for the songs file.
	 */
	public static String getSongsFileName(String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return getDefaultSongsFileName();
		} else {
			return fileName;
		}
	}
	
	public static String getDefaultDBFileName() {
		return getDBDir() + File.separator + DB_FILE_STRING;
	}
	
	public static String getDefaultDBPropertiesFileName() {
		return getDBDir() + File.separator + DB_PROPERTIES_FILE_STRING;
	}
	
	public static String getSettingsFileName() {
		return getSettingsDir() + File.separator + SETTINGS_FILE_STRING;
	}
	
	public static String getSettingsFallbackFileName() {
		return getSettingsDir() + File.separator + SETTINGS_FALLBACK_FILE_STRING;
	}
	
	public static String getStatisticsFileName() {
		return getStatisticsDir() + File.separator + STATISTICS_FILE_STRING;
	}
	
	/** this is used when remote control (via MQTT) is active */
	private static String getDBDir() {
		if (Options.getInstance().getDatabaseDir() == null) {
			return getDir(DB_SUBDIR_STRING, true);
		} else {
			return getDir(Options.getInstance().getDatabaseDir(), false);
		}
	}
	
	private static String getSongsDir() {
		if (Options.getInstance().getSongsDir() == null) {
			return getDir(SONGS_SUBDIR_STRING, true);
		} else {
			return getDir(Options.getInstance().getSongsDir(), false);
		}
	}
	
	public static String getSongsBackupDir() {
		return getDir(getSongsDir() + File.separator + SONGS_BACKUP_SUBDIR_STRING, false);
	}
	
	public static String getSongsBackupFile() {
		return getSongsBackupDir() + File.separator + String.format(SONGS_BACKUP_FILE_STRING, DateTools.timestamp());
	}
	
	private static String getSettingsDir() {
		if (Options.getInstance().getSettingsDir() == null) {
			return getDir(SETTINGS_SUBDIR_STRING, true);
		} else {
			return getDir(Options.getInstance().getSettingsDir(), false);
		}
	}
	
	private static String getStatisticsDir() {
		if (Options.getInstance().getStatisticsDir() == null) {
			return getDir(STATISTICS_SUBDIR_STRING, true);
		} else {
			return getDir(Options.getInstance().getStatisticsDir(), false);
		}
	}
	
	/**
	 * This method also provides the actual log directory to Logback (via {@code Options.getPropertyValue()}).
	 */
	public static String getLogDir() {
		if (Options.getInstance().getLogsDir() == null) {
			return getDir(LOG_SUBDIR_STRING, true);
		} else {
			return getDir(Options.getInstance().getLogsDir(), false);
		}
	}
	
	private static String getDir(String subDirectory, boolean prependBaseDir) {
		String path = (prependBaseDir ? BASE_DIR_STRING + File.separator : "") + subDirectory;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return path;
	}
}
