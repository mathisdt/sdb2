package org.zephyrsoft.sdb2;

import org.zephyrsoft.sdb2.gui.*;
import org.zephyrsoft.sdb2.model.*;

/**
 * Controller for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainController {
	
	private MainModel model;
	
	public boolean prepareClose() {
		// TODO
		return true;
	}
	
	public void shutdown() {
		System.exit(0);
	}
	
	public void initializeModel() {
		model = new MainModel();
		// TODO load from file
		
		// TODO set basic values if necessary
		
	}
	
	public MainModel getModel() {
		return model;
	}
	
}
