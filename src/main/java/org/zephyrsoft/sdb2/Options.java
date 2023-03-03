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

import java.util.Properties;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Container for command line options.
 */
public enum Options {
	INSTANCE;
	
	public static Options getInstance() {
		return INSTANCE;
	}
	
	@Option(name = "--help", aliases = { "-help", "-h" }, usage = "display a short description of the available command line options (this message)")
	private boolean help = false;
	
	@Option(name = "--properties", aliases = "-props", metaVar = "<FILE>", usage = "use this file as configuration source")
	private String propertiesFile = FileAndDirectoryLocations.getDefaultPropertiesFileName();
	
	private static final String PROP_LANGUAGE = "language";
	@Option(name = "--language", aliases = "-lang", metaVar = "<CODE>", usage = "use this language (2-char ISO code, e.g. de)")
	private String language = null;
	
	private static final String PROP_COUNTRY = "country";
	@Option(name = "--country", metaVar = "<CODE>", usage = "use this country (2-char ISO code, e.g. DE)")
	private String country = null;
	
	private static final String PROP_TIMEZONE = "timezone";
	@Option(name = "--timezone", aliases = "-tz", metaVar = "<NAME>", usage = "use this time zone (e.g. Europe/Berlin)")
	private String timezone = null;
	
	private static final String PROP_STATISTICS_DIR = "statistics";
	@Option(name = "--statistics", aliases = "-stat", metaVar = "<DIR>", usage = "use this directory as statistics storage (optional, the default is ~/.songdatabase/statistics/)")
	private String statisticsDir = null;
	
	private static final String PROP_SONGS_DIR = "songs";
	@Option(name = "--songs", aliases = "-song", metaVar = "<DIR>", usage = "use this directory as songs storage (optional, the default is ~/.songdatabase/songs/)")
	private String songsDir = null;
	
	private static final String PROP_SONGS_BACKUP_DIR = "songs-backup";
	@Option(name = "--songs-backup", aliases = "-songback", metaVar = "<DIR>", usage = "use this directory as songs backup storage (optional, the default is ~/.songdatabase/songs/backup/)")
	private String songsBackupDir = null;
	
	private static final String PROP_SETTINGS_DIR = "settings";
	@Option(name = "--settings", aliases = "-sett", metaVar = "<DIR>", usage = "use this directory as settings storage (optional, the default is ~/.songdatabase/settings/)")
	private String settingsDir = null;
	
	private static final String PROP_LOGS_DIR = "logs";
	@Option(name = "--logs", aliases = "-logs", metaVar = "<DIR>", usage = "use this directory as logs storage (optional, the default is ~/.songdatabase/log/)")
	private String logsDir = null;
	
	private static final String PROP_LOGS_ROLLOVER_DIR = "logs-rollover";
	@Option(name = "--logs-rollover", aliases = "-logsroll", metaVar = "<DIR>", usage = "use this directory as older logs storage (optional, the default is ~/.songdatabase/log/)")
	private String logsRolloverDir = null;
	
	private static final String PROP_DATABASE_DIR = "database";
	/** this is used when remote control (via MQTT) is active */
	@Option(name = "--database", aliases = "-db", metaVar = "<DIR>", usage = "use this directory as database storage (optional, the default is ~/.songdatabase/db/)")
	private String databaseDir = null;
	
	private static final String PROP_SONGS_FILE = "songs-file";
	@Argument(metaVar = "<FILE>", usage = "use this file to load from and save to (optional, the default is ~/.songdatabase/songs/songs.xml)")
	private String songsFile = null;
	
	public boolean isHelp() {
		return help;
	}
	
	void setHelp(boolean help) {
		this.help = help;
	}
	
	public String getPropertiesFile() {
		return propertiesFile;
	}
	
	private void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}
	
	public String getLanguage() {
		return language;
	}
	
	private void setLanguage(String language) {
		this.language = language;
	}
	
	public String getCountry() {
		return country;
	}
	
	private void setCountry(String country) {
		this.country = country;
	}
	
	public String getTimezone() {
		return timezone;
	}
	
	private void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	public String getStatisticsDir() {
		return statisticsDir;
	}
	
	private void setStatisticsDir(String statisticsDir) {
		this.statisticsDir = statisticsDir;
	}
	
	public String getSongsDir() {
		return songsDir;
	}
	
	private void setSongsDir(String songsDir) {
		this.songsDir = songsDir;
	}
	
	public String getSongsBackupDir() {
		return songsBackupDir;
	}
	
	private void setSongsBackupDir(String songsBackupDir) {
		this.songsBackupDir = songsBackupDir;
	}
	
	public String getSettingsDir() {
		return settingsDir;
	}
	
	private void setSettingsDir(String settingsDir) {
		this.settingsDir = settingsDir;
	}
	
	public String getLogsDir() {
		return logsDir;
	}
	
	private void setLogsDir(String logsDir) {
		this.logsDir = logsDir;
	}
	
	public String getLogsRolloverDir() {
		return logsRolloverDir;
	}
	
	private void setLogsRolloverDir(String logsRolloverDir) {
		this.logsRolloverDir = logsRolloverDir;
	}
	
	public String getDatabaseDir() {
		return databaseDir;
	}
	
	private void setDatabaseDir(String databaseDir) {
		this.databaseDir = databaseDir;
	}
	
	public String getSongsFile() {
		return songsFile;
	}
	
	private void setSongsFile(String songsFile) {
		this.songsFile = songsFile;
	}
	
	/**
	 * Only uses the value from the properties file if no value was provided via a direct command line option.
	 */
	void addMissingValuesFrom(Properties props) {
		if (language == null && props.containsKey(PROP_LANGUAGE)) {
			setLanguage(props.getProperty(PROP_LANGUAGE));
		}
		if (country == null && props.containsKey(PROP_COUNTRY)) {
			setCountry(props.getProperty(PROP_COUNTRY));
		}
		if (timezone == null && props.containsKey(PROP_TIMEZONE)) {
			setTimezone(props.getProperty(PROP_TIMEZONE));
		}
		if (statisticsDir == null && props.containsKey(PROP_STATISTICS_DIR)) {
			setStatisticsDir(props.getProperty(PROP_STATISTICS_DIR));
		}
		if (songsDir == null && props.containsKey(PROP_SONGS_DIR)) {
			setSongsDir(props.getProperty(PROP_SONGS_DIR));
		}
		if (songsBackupDir == null && props.containsKey(PROP_SONGS_BACKUP_DIR)) {
			setSongsBackupDir(props.getProperty(PROP_SONGS_BACKUP_DIR));
		}
		if (settingsDir == null && props.containsKey(PROP_SETTINGS_DIR)) {
			setSettingsDir(props.getProperty(PROP_SETTINGS_DIR));
		}
		if (logsDir == null && props.containsKey(PROP_LOGS_DIR)) {
			setLogsDir(props.getProperty(PROP_LOGS_DIR));
		}
		if (logsRolloverDir == null && props.containsKey(PROP_LOGS_ROLLOVER_DIR)) {
			setLogsRolloverDir(props.getProperty(PROP_LOGS_ROLLOVER_DIR));
		}
		if (databaseDir == null && props.containsKey(PROP_DATABASE_DIR)) {
			setDatabaseDir(props.getProperty(PROP_DATABASE_DIR));
		}
		if (songsFile == null && props.containsKey(PROP_SONGS_FILE)) {
			setSongsFile(props.getProperty(PROP_SONGS_FILE));
		}
	}
}
