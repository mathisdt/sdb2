package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.MainModel;
import org.zephyrsoft.sdb2.model.XMLConverter;

/**
 * Controller for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainController {
	
	public static final String DATA_DIR_STRING = System.getProperty("user.home") + File.separator + ".songdatabase";
	public static final String DATA_FILE_STRING = "songs.sdb2";
	
	private static Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	private String dbFileName = null;
	private MainModel model = null;
	
	public boolean prepareClose() {
		LOG.debug("preparing to close application");
		boolean successfullySaved = serializeToFile();
		return successfullySaved;
	}
	
	public void shutdown() {
		shutdown(0);
	}
	
	public void shutdown(int exitCode) {
		LOG.debug("closing application, exit code " + exitCode);
		System.exit(exitCode);
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
		if (dbFileName == null) {
			return getDefaultDataDir() + File.separator + DATA_FILE_STRING;
		} else {
			return dbFileName;
		}
	}
	
	private String getDefaultDataDir() {
		File dataDir = new File(DATA_DIR_STRING);
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		return DATA_DIR_STRING;
	}
	
	/**
	 * Sets the database location to a custom value. Must be called before {@link #populateFromFile()} to be effective!
	 */
	public void setDbFileName(String dbFileName) {
		this.dbFileName = dbFileName;
	}
	
	/**
	 * Use a nice LaF.
	 * 
	 * @return {@code true} if the LaF could be applied, {@code false} otherwise
	 */
	public boolean setupLookAndFeel() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
			| UnsupportedLookAndFeelException e) {
			LOG.warn("could not apply the look-and-feel");
			return false;
		}
	}
	
}
