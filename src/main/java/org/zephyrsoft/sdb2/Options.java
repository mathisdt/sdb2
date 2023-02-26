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
	
	@Option(name = "--statistics", aliases = "-stat", metaVar = "<DIR>", usage = "use this directory as statistics storage (optional, the default is ~/.songdatabase/statistics/)")
	private String statisticsDir = null;
	
	@Option(name = "--songs", aliases = "-song", metaVar = "<DIR>", usage = "use this directory as songs storage (optional, the default is ~/.songdatabase/songs/)")
	private String songsDir = null;
	
	@Option(name = "--songs-backup", aliases = "-songback", metaVar = "<DIR>", usage = "use this directory as songs backup storage (optional, the default is ~/.songdatabase/songs/backup/)")
	private String songsBackupDir = null;
	
	@Option(name = "--settings", aliases = "-sett", metaVar = "<DIR>", usage = "use this directory as settings storage (optional, the default is ~/.songdatabase/settings/)")
	private String settingsDir = null;
	
	@Option(name = "--logs", aliases = "-logs", metaVar = "<DIR>", usage = "use this directory as logs storage (optional, the default is ~/.songdatabase/log/)")
	private String logsDir = null;
	
	/** this is used when remote control (via MQTT) is active */
	@Option(name = "--database", aliases = "-db", metaVar = "<DIR>", usage = "use this directory as database storage (optional, the default is ~/.songdatabase/db/)")
	private String databaseDir = null;
	
	@Argument(metaVar = "<FILE>", usage = "use this file to load from and save to (optional, the default is ~/.songdatabase/songs/songs.xml)")
	private String songsFile = null;
	
	public boolean isHelp() {
		return help;
	}
	
	public void setHelp(boolean help) {
		this.help = help;
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
}
