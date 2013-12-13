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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zephyrsoft.sdb2.gui.MainWindow;

/**
 * Startup class for SDBv2.
 * 
 * @author Mathis Dirksen-Thedens
 */
public final class Start {

	private static final Logger LOG = LoggerFactory.getLogger(Start.class);

	private final Options options = new Options();

	private MainController mainController;

	private MainWindow mainWindow;

	public static void main(String[] args) {
		new Start(args);
	}

	private Start(String[] args) {
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
				LOG.info("loading application context");
				@SuppressWarnings("resource")
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
				context.register(SpringConfiguration.class);
				context.refresh();

				context.getAutowireCapableBeanFactory().autowireBean(this);
				mainController.loadSongs(options.getSongsFile());

				mainWindow.startup();
			} catch (Exception e) {
				LOG.error("problem while starting up the application", e);
				System.exit(-1);
			}
		}
	}

	@Autowired
	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	@Autowired
	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

}
