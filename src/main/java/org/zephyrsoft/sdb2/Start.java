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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.util.StringTools;

/**
 * Startup class for SDBv2.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class Start {
	
	private static final Logger LOG = LoggerFactory.getLogger(Start.class);
	
	@Option(name = "--help", aliases = {"-help", "-h"},
		usage = "display a short description of the available command line options (this message)")
	private boolean help = false;
	
	@Argument(metaVar = "<FILE>",
		usage = "use this file to load from and save to (optional, the default is ~/.songdatabase/songs/songs.xml)")
	private String songsFile = null;
	
	public static void main(String[] args) {
		new Start(args);
	}
	
	public Start(String[] args) {
		LOG.debug("starting application");
		
		// parse command line arguments
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			help = true;
		}
		
		if (help) {
			System.err.println("The available options are:");
			parser.printUsage(System.err);
		} else {
			MainController controller = new MainController();
			controller.setupLookAndFeel();
			try {
				MainWindow window = new MainWindow(controller);
				controller.loadSettings();
				controller.loadStatistics();
				if (!StringTools.isBlank(songsFile)) {
					controller.setSongsFileName(songsFile);
				}
				controller.initializeSongsModel();
				window.setModels(controller.getSongs(), controller.getSettings());
				window.setVisible(true);
			} catch (Throwable t) {
				LOG.error("problem while starting up the application", t);
				controller.shutdown(-1);
			}
		}
	}
	
}
