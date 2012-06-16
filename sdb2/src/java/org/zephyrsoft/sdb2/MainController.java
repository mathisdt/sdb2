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
import org.zephyrsoft.sdb2.model.SettingsModel;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;

/**
 * Controller for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainController {
	
	public static final String BASE_DIR_STRING = System.getProperty("user.home") + File.separator + ".songdatabase";
	public static final String SONGS_SUBDIR_STRING = "songs";
	public static final String SONGS_FILE_STRING = "songs.xml";
	public static final String SETTINGS_SUBDIR_STRING = "settings";
	public static final String SETTINGS_FILE_STRING = "settings.xml";
	
	private static Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	private String songsFileName = null;
	private SongsModel songs = null;
	private SettingsModel settings = null;
	
	public boolean prepareClose() {
		LOG.debug("preparing to close application");
		boolean successfullySavedSongs = saveSongs();
		boolean successfullySavedSettings = saveSettings();
		return successfullySavedSongs && successfullySavedSettings;
	}
	
	public void shutdown() {
		shutdown(0);
	}
	
	public void shutdown(int exitCode) {
		LOG.debug("closing application, exit code " + exitCode);
		System.exit(exitCode);
	}
	
	public void initializeSongsModel() {
		LOG.debug("loading songs from file");
		songs = populateSongsModelFromFile();
		if (songs == null) {
			// there was a problem while reading
			songs = new SongsModel();
		}
	}
	
	public void loadSettings() {
		LOG.debug("loading settings from file");
		File file = new File(getSettingsFileName());
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			settings = XMLConverter.fromXMLToSettingsModel(xmlInputStream);
			xmlInputStream.close();
		} catch (IOException e) {
			LOG.error("could not read settings from \"" + file.getAbsolutePath() + "\"");
		}
		if (settings == null) {
			// there was a problem while reading
			settings = new SettingsModel();
		}
	}
	
	public boolean saveSettings() {
		File file = new File(getSettingsFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromSettingsModelToXML(settings, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write settings to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	private SongsModel populateSongsModelFromFile() {
		File file = new File(getSongsFileName());
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			SongsModel modelToReturn = XMLConverter.fromXMLToSongsModel(xmlInputStream);
			xmlInputStream.close();
			return modelToReturn;
		} catch (IOException e) {
			LOG.error("could not read songs from \"" + file.getAbsolutePath() + "\"");
			return null;
		}
	}
	
	private boolean saveSongs() {
		File file = new File(getSongsFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromSongsModelToXML(songs, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write songs to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	public SongsModel getSongs() {
		return songs;
	}
	
	public SettingsModel getSettings() {
		return settings;
	}
	
	private String getSongsFileName() {
		if (songsFileName == null) {
			return getSongsDir() + File.separator + SONGS_FILE_STRING;
		} else {
			return songsFileName;
		}
	}
	
	private String getSettingsFileName() {
		return getSettingsDir() + File.separator + SETTINGS_FILE_STRING;
	}
	
	private String getSongsDir() {
		return getDir(SONGS_SUBDIR_STRING);
	}
	
	private String getSettingsDir() {
		return getDir(SETTINGS_SUBDIR_STRING);
	}
	
	private String getDir(String subDirectory) {
		String path = BASE_DIR_STRING + File.separator + subDirectory;
		File dataDir = new File(path);
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		return path;
	}
	
	/**
	 * Sets the database location to a custom value. Must be called before {@link #populateSongsModelFromFile()} to be
	 * effective!
	 */
	public void setSongsFileName(String dbFileName) {
		this.songsFileName = dbFileName;
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
