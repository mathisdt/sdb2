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

import java.nio.charset.Charset;
import java.time.ZoneId;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;

/**
 * Startup class for SDBv2.
 */
public final class Start {
	
	private static final Logger LOG = LoggerFactory.getLogger(Start.class);
	
	public static void main(String[] args) {
		new Start(args);
	}
	
	@SuppressWarnings("resource")
	private Start(String[] args) {
		LOG.debug("starting application");
		
		Options options = Options.getInstance();
		
		// parse command line arguments
		CmdLineParser parser = new CmdLineParser(options);
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
				LOG.debug("default file encoding is {}", Charset.defaultCharset().displayName());
				LOG.debug("default time zone is {}", ZoneId.systemDefault().getId());
				
				LOG.debug("loading application context");
				new AnnotationConfigApplicationContext(SpringConfiguration.class);
			} catch (Exception e) {
				LOG.error("problem while starting up the application", e);
				ErrorDialog.openDialogBlocking(null, "There was a problem while starting the Song Database:\n\n"
					+ e.getMessage()
					+ "\n\nThis is a fatal error, exiting.\n"
					+ "Please see the log file for more details.");
				System.exit(-1);
			}
		}
	}
	
}
