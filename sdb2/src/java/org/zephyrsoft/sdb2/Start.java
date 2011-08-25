package org.zephyrsoft.sdb2;

import java.awt.*;
import org.slf4j.*;
import org.zephyrsoft.sdb2.gui.*;

public class Start {
	
	private static Logger LOG = LoggerFactory.getLogger(Start.class);
	
	public static void main(String[] args) {
		LOG.debug("starting application");
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainController controller = new MainController();
					MainWindow window = new MainWindow(controller);
					controller.initializeModel();
					window.setModel(controller.getModel());
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
