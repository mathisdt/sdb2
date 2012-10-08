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

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.Presenter;
import org.zephyrsoft.sdb2.presenter.PresenterBundle;
import org.zephyrsoft.sdb2.presenter.PresenterWindow;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;
import org.zephyrsoft.sdb2.presenter.Scroller;
import org.zephyrsoft.util.gui.ErrorDialog;

/**
 * Controller for {@link MainWindow}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainController implements Scroller {
	
	private static Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	private String songsFileName = null;
	private SongsModel songs = null;
	private SettingsModel settings = null;
	private StatisticsModel statistics = null;
	private List<GraphicsDevice> screens;
	private PresenterBundle presentationControl;
	private Song currentlyPresentedSong = null;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Future<?> countDownFuture;
	
	public boolean present(Presentable presentable) {
		// end old presentation (if any)
		if (presentationControl != null) {
			presentationControl.hidePresenter();
		}
		
		presentationControl = new PresenterBundle();
		
		Presenter presenter1 =
			createPresenter(ScreenHelper.getScreen(screens, settings.getString(SettingKey.SCREEN_1_DISPLAY)),
				presentable, (ScreenContentsEnum) settings.get(SettingKey.SCREEN_1_CONTENTS));
		if (presenter1 != null) {
			presentationControl.addPresenter(presenter1);
		}
		
		Presenter presenter2 =
			createPresenter(ScreenHelper.getScreen(screens, settings.getString(SettingKey.SCREEN_2_DISPLAY)),
				presentable, (ScreenContentsEnum) settings.get(SettingKey.SCREEN_2_CONTENTS));
		if (presenter2 != null) {
			presentationControl.addPresenter(presenter2);
		}
		
		if (presentationControl.size() == 0) {
			ErrorDialog
				.openDialog(null,
					"You have to specify at least one presentation display.\n\nPlease review your \"Global Settings\" tab!");
			return false;
		} else {
			currentlyPresentedSong = presentable.getSong();
			
			if (currentlyPresentedSong != null) {
				startCountDown(settings.getInteger(SettingKey.SECONDS_UNTIL_COUNTED), currentlyPresentedSong);
			} else {
				stopCountDown();
			}
			
			// start presentation
			presentationControl.showPresenter();
			
			return true;
		}
	}
	
	public void startCountDown(final int seconds, final Song song) {
		Runnable countDownRunnable = new Runnable() {
			@Override
			public void run() {
				LOG.debug("start sleeping for {} seconds", seconds);
				try {
					Thread.sleep(seconds * 1000);
					countSongAsPresentedToday(song);
				} catch (InterruptedException e) {
					// if interrupted, do nothing (the countdown was stopped)
					LOG.debug("interrupted");
				}
			}
		};
		stopCountDown();
		countDownFuture = executor.submit(countDownRunnable);
	}
	
	public void stopCountDown() {
		if (countDownFuture != null) {
			LOG.debug("stopping countdown");
			countDownFuture.cancel(true);
			countDownFuture = null;
		} else {
			LOG.debug("wanted to stop countdown, but nothing to do");
		}
	}
	
	@Override
	public List<AddressablePart> getParts() {
		Validate.notNull(presentationControl, "there is no active presentation");
		return presentationControl.getParts();
	}
	
	@Override
	public void moveToPart(Integer part) {
		presentationControl.moveToPart(part);
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		presentationControl.moveToLine(part, line);
	}
	
	private PresenterWindow createPresenter(GraphicsDevice screen, Presentable presentable, ScreenContentsEnum contents) {
		if (screen == null) {
			// nothing to be done
			return null;
		}
		return new PresenterWindow(screen, presentable, contents, settings);
	}
	
	public List<GraphicsDevice> getScreens() {
		return Collections.unmodifiableList(screens);
	}
	
	public void detectScreens() {
		if (screens == null) {
			screens = new ArrayList<>();
		} else {
			screens.clear();
		}
		
		// for the setting "don't show at all"
		screens.add(null);
		
		List<GraphicsDevice> screensInternal = ScreenHelper.getScreens();
		for (GraphicsDevice screen : screensInternal) {
			screens.add(screen);
		}
	}
	
	public boolean prepareClose() {
		LOG.debug("preparing to close application");
		return saveAll();
	}
	
	public boolean saveAll() {
		boolean successfullySavedSongs = saveSongs();
		boolean successfullySavedSettings = saveSettings();
		boolean successfullySavedStatistics = saveStatistics();
		return successfullySavedSongs && successfullySavedSettings && successfullySavedStatistics;
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
	
	public void loadStatistics() {
		LOG.debug("loading statistics from file");
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			statistics = XMLConverter.fromXMLToStatisticsModel(xmlInputStream);
			xmlInputStream.close();
		} catch (IOException e) {
			LOG.error("could not read statistics from \"" + file.getAbsolutePath() + "\"");
		}
		if (statistics == null) {
			// there was a problem while reading
			statistics = new StatisticsModel();
		}
	}
	
	public void loadSettings() {
		LOG.debug("loading settings from file");
		File file = new File(FileAndDirectoryLocations.getSettingsFileName());
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
		loadDefaultSettingsForUnsetSettings();
	}
	
	private void loadDefaultSettingsForUnsetSettings() {
		putDefaultIfKeyIsUnset(SettingKey.BACKGROUND_COLOR, Color.BLACK);
		putDefaultIfKeyIsUnset(SettingKey.TEXT_COLOR, Color.WHITE);
		
		putDefaultIfKeyIsUnset(SettingKey.TOP_MARGIN, Integer.valueOf(10));
		putDefaultIfKeyIsUnset(SettingKey.LEFT_MARGIN, Integer.valueOf(0));
		putDefaultIfKeyIsUnset(SettingKey.RIGHT_MARGIN, Integer.valueOf(0));
		putDefaultIfKeyIsUnset(SettingKey.BOTTOM_MARGIN, Integer.valueOf(20));
		putDefaultIfKeyIsUnset(SettingKey.DISTANCE_TITLE_TEXT, Integer.valueOf(20));
		putDefaultIfKeyIsUnset(SettingKey.DISTANCE_TEXT_COPYRIGHT, Integer.valueOf(20));
		
		putDefaultIfKeyIsUnset(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.TITLE_AND_LYRICS);
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.ONLY_LYRICS);
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_1_DISPLAY, "");
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.LYRICS_AND_CHORDS);
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_2_DISPLAY, "");
		
		putDefaultIfKeyIsUnset(SettingKey.SHOW_TITLE, Boolean.TRUE);
		putDefaultIfKeyIsUnset(SettingKey.TITLE_FONT, new Font(Font.SERIF, Font.BOLD, 10));
		putDefaultIfKeyIsUnset(SettingKey.LYRICS_FONT, new Font(Font.SERIF, Font.PLAIN, 10));
		putDefaultIfKeyIsUnset(SettingKey.TRANSLATION_FONT, new Font(Font.SERIF, Font.PLAIN, 10));
		putDefaultIfKeyIsUnset(SettingKey.COPYRIGHT_FONT, new Font(Font.SERIF, Font.ITALIC, 10));
		putDefaultIfKeyIsUnset(SettingKey.LOGO_FILE, "");
		putDefaultIfKeyIsUnset(SettingKey.SECONDS_UNTIL_COUNTED, Integer.valueOf(60));
		
		// check that really all settings are set
		for (SettingKey key : SettingKey.values()) {
			if (!settings.isSet(key)) {
				throw new IllegalStateException("unset value for setting key: " + key);
			}
		}
	}
	
	private void putDefaultIfKeyIsUnset(SettingKey key, Object defaultValue) {
		if (!settings.isSet(key)) {
			settings.put(key, defaultValue);
		}
	}
	
	public void countSongAsPresentedToday(Song song) {
		Validate.notNull(song, "counted song must be different from null");
		LOG.info("counting song \"{}\" as presented today", song.getTitle());
		statistics.addStatisticsEntry(song, new Date());
	}
	
	public synchronized boolean saveStatistics() {
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromStatisticsModelToXML(statistics, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write statistics to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	public synchronized boolean saveSettings() {
		File file = new File(FileAndDirectoryLocations.getSettingsFileName());
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
	
	public synchronized boolean saveSongs() {
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
			return FileAndDirectoryLocations.getDefaultSongsFileName();
		} else {
			return songsFileName;
		}
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
	
	public Song getCurrentlyPresentedSong() {
		return currentlyPresentedSong;
	}
	
}
