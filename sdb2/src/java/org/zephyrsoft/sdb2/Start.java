package org.zephyrsoft.sdb2;

import org.kohsuke.args4j.*;
import org.slf4j.*;
import org.zephyrsoft.sdb2.gui.*;

public class Start {
	
	private static Logger LOG = LoggerFactory.getLogger(Start.class);
	
	@Option(name = "--help", aliases = {"-help", "-h"},
		usage = "display a short description of the available command line options (this message)")
	private boolean help = false;
	@Option(name = "--dbfile", metaVar = "FILE",
		usage = "use this instead of the default location to load and save the song database")
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
			parser.printUsage(System.err);
		}
		
		if (help) {
			System.err.println("The available options are:");
			parser.printUsage(System.err);
		} else {
			MainController controller = new MainController();
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
