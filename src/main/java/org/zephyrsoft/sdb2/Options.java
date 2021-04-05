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
	
	@Argument(metaVar = "<FILE>", usage = "use this file to load from and save to (optional, the default is ~/.songdatabase/songs/songs.xml)")
	private String songsFile = null;
	
	public boolean isHelp() {
		return help;
	}
	
	public void setHelp(boolean help) {
		this.help = help;
	}
	
	public String getSongsFile() {
		return songsFile;
	}
	
	public void setSongsFile(String songsFile) {
		this.songsFile = songsFile;
	}
	
}
