package org.zephyrsoft.sdb2;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;

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
		usage = "use this file to load from and save to (optional, the default is ~/.songdatabase/songs.sdb2)")
	private String databaseFile = null;
	
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
				if (databaseFile != null) {
					controller.setDbFileName(databaseFile);
				}
				controller.initializeModel();
				window.setModel(controller.getModel());
				window.setVisible(true);
			} catch (Throwable t) {
				LOG.error("problem while starting up the application", t);
				controller.shutdown(-1);
			}
		}
	}
	
}
