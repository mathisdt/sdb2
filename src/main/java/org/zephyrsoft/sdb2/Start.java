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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Startup class for SDBv2.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class Start {

	private static final Logger LOG = LoggerFactory.getLogger(Start.class);

	private static Options options = new Options();

	public static void main(String[] args) {
		LOG.debug("starting application");

		// parse command line arguments
		CmdLineParser parser = new CmdLineParser(options);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			options.setHelp(true);
		}

		if (options.isHelp()) {
			System.err.println("The available options are:");
			parser.printUsage(System.err);
		} else {
			try {
				// put command line option into system properties (for Spring)
				if (options.getSongsFile() != null) {
					System.setProperty(Options.SONGS_FILE, options.getSongsFile());
				}

				LOG.info("loading application context");
				@SuppressWarnings("resource")
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
				context.register(SpringConfiguration.class);
				context.refresh();
			} catch (Throwable t) {
				LOG.error("problem while starting up the application", t);
				System.exit(-1);
			}
		}
	}

	public static String getSongsFile() {
		return options.getSongsFile();
	}

}
