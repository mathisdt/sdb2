package org.zephyrsoft.sdb2;

import java.io.*;
import org.slf4j.*;
import org.zephyrsoft.sdb2.gui.*;
import org.zephyrsoft.sdb2.model.*;

/**
 * Controller for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainController {
	
	private static Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	private String dbFileName = null;
	private MainModel model = null;
	
	public boolean prepareClose() {
		LOG.debug("preparing to close application");
		boolean successfullySaved = serializeToFile();
		return successfullySaved;
	}
	
	public void shutdown() {
		LOG.debug("closing application");
		System.exit(0);
	}
	
	public void initializeModel() {
		LOG.debug("initializing model from file");
		model = populateFromFile();
		if (model == null) {
			// there was a problem while reading
			model = new MainModel();
		}
	}
	
	private MainModel populateFromFile() {
		File file = new File(getDbFileName());
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			MainModel modelToReturn = XMLConverter.fromXMLToModel(xmlInputStream);
			xmlInputStream.close();
			return modelToReturn;
		} catch (IOException e) {
			LOG.error("could not read data from \"" + file.getAbsolutePath() + "\"");
			return null;
		}
	}
	
	private boolean serializeToFile() {
		File file = new File(getDbFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromModelToXML(model, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write data to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	public MainModel getModel() {
		return model;
	}
	
	private String getDbFileName() {
		if (dbFileName==null) {
			return getDefaultDataDir() + File.separator + "songs.sdb2";
		} else {
			return dbFileName;
		}
	}
	
	private String getDefaultDataDir() {
		String dataDirString = System.getProperty("user.home") + File.separator + ".songdatabase";
		File dataDir = new File(dataDirString);
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		return dataDirString;
	}
	
	/**
	 * Sets the database location to a custom value. Must be called before {@link #populateFromFile()} to be effective!
	 */
	public void setDbFileName(String dbFileName) {
		this.dbFileName = dbFileName;
	}
	
}
