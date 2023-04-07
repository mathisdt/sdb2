/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SelectableDisplay;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.SongsModelController;
import org.zephyrsoft.sdb2.model.VirtualScreen;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.Presenter;
import org.zephyrsoft.sdb2.presenter.PresenterBundle;
import org.zephyrsoft.sdb2.presenter.PresenterWindow;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;
import org.zephyrsoft.sdb2.presenter.Scroller;
import org.zephyrsoft.sdb2.remote.Health;
import org.zephyrsoft.sdb2.remote.MQTT;
import org.zephyrsoft.sdb2.remote.PatchController;
import org.zephyrsoft.sdb2.remote.RemoteController;
import org.zephyrsoft.sdb2.remote.RemotePresenter;
import org.zephyrsoft.sdb2.remote.RemoteStatus;
import org.zephyrsoft.sdb2.remote.SDB2RemotePreferences;
import org.zephyrsoft.sdb2.util.StringTools;
import org.zephyrsoft.sdb2.util.calendar.ICalInterpreter;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 * Controller for {@link MainWindow}.
 */
public class MainController implements Scroller {
	
	private static final String PRESENTATION_DISPLAY_WARNING = """
		Could not start presentation!
		
		Please specify at least one existing presentation display:
		Check your system configuration
		and/or adjust this program's configuration
		(see tab "Global Settings")!""";
	
	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	private Executor contentChanger = Executors.newSingleThreadExecutor();
	
	private MainWindow mainWindow;
	
	private final IOController ioController;
	private final StatisticsController statisticsController;
	private RemoteController remoteController;
	
	private String songsFileName = FileAndDirectoryLocations.getDefaultSongsFileName();
	private final SongsModel songs = new SongsModel(true);
	private SongsModelController songsController = new SongsModelController(songs);
	private SettingsModel settings = null;
	
	private List<SelectableDisplay> screens;
	private PresenterBundle presentationControl;
	private Song currentlyPresentedSong = null;
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	private Future<?> countDownFuture;
	private Iterator<File> slideShowImages;
	private Future<?> slideShowFuture;
	private final static Pattern imagePattern = Pattern.compile("(?i)^.*\\.(png|jpg|jpeg|gif|bmp)$");
	
	private Thread shutdownHook = null;
	
	private MQTT mqtt;
	
	private SDB2RemotePreferences remotePreferences;
	
	public MainController(IOController ioController, StatisticsController statisticsController) {
		this.ioController = ioController;
		this.statisticsController = statisticsController;
	}
	
	/**
	 * Will initialize a remote-controller instance or create a new one, if it is already initialized.
	 * It will also replace the songsController, if a database service is online.
	 * Run this function in a separate Thread to not block the UI and to see status changes.
	 */
	public void initRemoteController() {
		assert settings != null : "Settings must be load before calling initRemoteController";
		
		closeRemoteController();
		
		if (settings.get(SettingKey.REMOTE_ENABLED, Boolean.class)) {
			setRemoteStatus(RemoteStatus.CONNECTING);
			remotePreferences = new SDB2RemotePreferences(settings);
			remoteController = new RemoteController(remotePreferences, this, mainWindow);
			remoteController.getHealthDB().onChange((h, args) -> {
				switch (h) {
					case online:
						if (!(songsController instanceof PatchController))
							songsController = new PatchController(songs, remoteController);
						break;
					case offline:
						if (!(songsController instanceof PatchController))
							break;
						SongsModelController oldController = songsController;
						songsController = new SongsModelController(songs);
						oldController.save();
						oldController.close();
						break;
				}
				setRemoteStatus(h == Health.online ? RemoteStatus.CONNECTED : RemoteStatus.DB_DISCONNECTED);
			});
			try {
				mqtt = new MQTT(remotePreferences.getServer(), remotePreferences.getClientID(), remotePreferences.getUsername(), remotePreferences
					.getPassword(), true);
				mqtt.onConnectionLost(cause -> {
					setRemoteStatus(RemoteStatus.FAILURE);
					cause.printStackTrace();
				});
				remoteController.connectTo(mqtt);
				
				setRemoteStatus(RemoteStatus.CONNECTED);
			} catch (MqttException e) {
				remoteController = null;
				mqtt = null;
				setRemoteStatus(RemoteStatus.FAILURE);
				ErrorDialog.openDialog(null, """
					Error while connecting to remote server.
					Please check settings or your network connection.
					Otherwise ask your system or network administrator.
					To reconnect, type Strg+R.""");
			}
		}
	}
	
	/**
	 * Called if settings changed out of mainwindow.
	 * Will recreate a remote-controller instance if remote settings changed.
	 */
	public void settingsChanged() {
		assert settings != null : "Settings must be load before calling settingsChanged";
		
		boolean enableChanged = settings.get(SettingKey.REMOTE_ENABLED, Boolean.class) != (remoteController != null);
		if (enableChanged || (remotePreferences != null && remotePreferences.equals(new SDB2RemotePreferences(settings))))
			new Thread(this::initRemoteController).start();
	}
	
	/** called from constructor of {@link MainWindow} */
	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	public void setRemoteStatus(RemoteStatus status) {
		if (mainWindow != null)
			mainWindow.setRemoteStatus(status);
	}
	
	public void contentChange(Runnable command) {
		contentChanger.execute(command);
	}
	
	public boolean present(Presentable presentable) {
		SelectableDisplay screen1 = ScreenHelper.getScreen(screens, settings.get(SettingKey.SCREEN_1_DISPLAY, Integer.class));
		SelectableDisplay screen2 = ScreenHelper.getScreen(screens, settings.get(SettingKey.SCREEN_2_DISPLAY, Integer.class));
		
		int screenPresentersConfigured = (settings.get(SettingKey.SCREEN_1_DISPLAY, Integer.class) != null ? 1 : 0)
			+ (settings.get(SettingKey.SCREEN_2_DISPLAY, Integer.class) != null ? 1 : 0);
		List<Presenter> screenPresenters = presentationControl == null
			? null
			: presentationControl.getScreenPresenters();
		
		if (presentationControl != null
			&& screenPresenters != null
			&& screenPresenters.size() == screenPresentersConfigured
			&& (screenPresenters.isEmpty() ||
				(screenPresenters.get(0) instanceof PresenterWindow pw
					&& pw.metadataMatches(screen1, VirtualScreen.SCREEN_A)
					&& pw.screenSizeMatches()))
			&& (screenPresenters.size() < 2 ||
				(screenPresenters.get(1) instanceof PresenterWindow pw
					&& pw.metadataMatches(screen2, VirtualScreen.SCREEN_B)
					&& pw.screenSizeMatches()))) {
			LOG.trace("re-using the existing presenters");
			currentlyPresentedSong = presentable.getSong();
			presentationControl.setContent(presentable);
			
			// the presentation windows were moved to front and got the focus because of that,
			// so we need to get the focus back to the main window:
			mainWindow.toFront();
			
			return true;
		} else {
			LOG.trace("using newly created presenters");
			return presentInNewPresenters(presentable, screen1, screen2);
		}
	}
	
	private boolean presentInNewPresenters(Presentable presentable, SelectableDisplay screen1, SelectableDisplay screen2) {
		PresenterBundle oldPresentationControl = presentationControl;
		presentationControl = new PresenterBundle();
		
		Presenter presenter1 = createPresenter(screen1, presentable, VirtualScreen.SCREEN_A);
		if (presenter1 != null) {
			presentationControl.addPresenter(presenter1);
		}
		
		Presenter presenter2 = createPresenter(screen2, presentable, VirtualScreen.SCREEN_B);
		if (presenter2 != null) {
			presentationControl.addPresenter(presenter2);
		}
		
		if (presentationControl.isEmpty()) {
			ErrorDialog
				.openDialog(
					null,
					PRESENTATION_DISPLAY_WARNING);
			return false;
		} else {
			if (remoteController != null) {
				presentationControl.addPresenter(remoteController.getRemotePresenter(presentable));
			}
			
			currentlyPresentedSong = presentable.getSong();
			
			if (currentlyPresentedSong != null) {
				startCountDown(settings.get(SettingKey.SECONDS_UNTIL_COUNTED, Integer.class), currentlyPresentedSong);
			} else {
				stopCountDown();
			}
			
			SwingUtilities.invokeLater(() -> {
				// start presentation
				try {
					presentationControl.showPresenter();
				} catch (IllegalStateException ise) {
					LOG.warn("could not show presenter window", ise);
					ErrorDialog
						.openDialog(
							null,
							PRESENTATION_DISPLAY_WARNING);
				}
				
				// now stop old presentation (if any), do not stop remote presenters
				if (oldPresentationControl != null) {
					oldPresentationControl.removeIf(p -> p instanceof RemotePresenter);
					oldPresentationControl.hidePresenter();
					oldPresentationControl.disposePresenter();
				}
			});
			
			return true;
		}
	}
	
	public void startCountDown(final int seconds, final Song song) {
		Runnable countDownRunnable = () -> {
			LOG.trace("start sleeping for {} seconds (count-down)", seconds);
			try {
				Thread.sleep(seconds * 1000);
				statisticsController.countSongAsPresentedToday(song);
			} catch (InterruptedException e) {
				// if interrupted, do nothing (the countdown was stopped)
				LOG.trace("interrupted (count-down)");
			}
		};
		stopCountDown();
		if (executor.isShutdown()) {
			throw new IllegalStateException("background executor is stopped");
		} else {
			countDownFuture = executor.submit(countDownRunnable);
		}
	}
	
	public void stopCountDown() {
		if (countDownFuture != null) {
			LOG.trace("stopping countdown");
			countDownFuture.cancel(true);
			countDownFuture = null;
		} else {
			LOG.trace("wanted to stop countdown, but nothing to do");
		}
	}
	
	@Override
	public List<AddressablePart> getParts() {
		Preconditions.checkArgument(presentationControl != null, "there is no active presentation");
		return presentationControl.getParts();
	}
	
	@Override
	public void moveToPart(Integer part) {
		if (MainController.this.presentationControl != null) {
			try {
				presentationControl.moveToPart(part);
			} catch (IllegalStateException e) {
				// if song is not displayed yet
			}
		}
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		if (MainController.this.presentationControl != null) {
			try {
				presentationControl.moveToLine(part, line);
			} catch (IllegalStateException e) {
				// if song is not displayed yet
			} catch (NullPointerException e) {
				// if song is not displayed yet
			}
		}
	}
	
	private PresenterWindow createPresenter(SelectableDisplay screen, Presentable presentable, VirtualScreen virtualScreen) {
		if (screen == null || !screen.isAvailable()) {
			// nothing to be done
			LOG.debug("could not create presenter window for virtual screen {} - display is {}", virtualScreen.getName(), screen == null ? "null"
				: "unavailable");
			return null;
		}
		PresenterWindow presenterWindow = null;
		try {
			presenterWindow = new PresenterWindow(screen, presentable, virtualScreen, settings, this);
		} catch (IllegalStateException ise) {
			LOG.warn("could not create presenter window", ise);
			return null;
		}
		
		return presenterWindow;
	}
	
	public List<SelectableDisplay> getScreens() {
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
		
		List<SelectableDisplay> availableScreens = ScreenHelper.getScreens();
		screens.addAll(availableScreens);
		
		// if applicable: add currently unavailable screens if they are mentioned in settings
		Integer screen1Index = settings.get(SettingKey.SCREEN_1_DISPLAY, Integer.class);
		Integer screen2Index = settings.get(SettingKey.SCREEN_2_DISPLAY, Integer.class);
		int maxScreenIndex = Math.max(screen1Index == null ? 0 : screen1Index, screen2Index == null ? 0 : screen2Index);
		for (int i = availableScreens.size(); i <= maxScreenIndex; i++) {
			LOG.debug("adding screen with index {} as unavailable", i);
			screens.add(new SelectableDisplay(i, false));
		}
	}
	
	private boolean disposePresenter() {
		if (presentationControl != null) {
			presentationControl.hidePresenter();
			presentationControl.disposePresenter();
		}
		return true;
	}
	
	public boolean closeRemoteController() {
		if (remoteController != null) {
			setRemoteStatus(RemoteStatus.DISCONNECTING);
			if (songsController instanceof PatchController) {
				songsController.close();
				songsController = new SongsModelController(songs);
			}
			if (presentationControl != null) {
				presentationControl.removeIf(p -> p instanceof RemotePresenter);
			}
			if (mqtt != null) {
				mqtt.close();
			}
			remoteController = null;
			mqtt = null;
			setRemoteStatus(RemoteStatus.OFF);
		}
		return true;
	}
	
	public boolean prepareClose() {
		LOG.debug("preparing to close application");
		return saveAll() && disposePresenter() && closeRemoteController();
	}
	
	public boolean saveAll() {
		boolean successfullySavedDB = songsController == null || songsController.save();
		boolean successfullySavedSongs = saveSongs();
		boolean successfullySavedSettings = saveSettings();
		boolean successfullySavedStatistics = statisticsController.saveStatistics();
		return successfullySavedDB && successfullySavedSongs && successfullySavedSettings && successfullySavedStatistics;
	}
	
	public void shutdown() {
		shutdown(0);
	}
	
	public void shutdown(int exitCode) {
		LOG.debug("closing application, exit code {}", exitCode);
		executor.shutdownNow();
		System.exit(exitCode);
	}
	
	public void loadSongs(String fileName) {
		if (!StringTools.isBlank(fileName)) {
			songsFileName = fileName;
		}
		SongsModel songsLoaded = populateSongsModel(songsFileName);
		if (songsLoaded != null) {
			songsController.update(songsLoaded);
		}
		
		if (shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
		shutdownHook = new Thread() {
			@Override
			public void run() {
				// don't use LOG here because the logger may already have shut down itself
				try {
					saveSongs();
					System.out.println("done saving songs on regular shutdown");
				} catch (Exception e) {
					System.err.println("could not save songs on regular shutdown: ");
					e.printStackTrace();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}
	
	public void startWatchingSongsFile() {
		try {
			LOG.info("starting to watch for changes in {}", songsFileName);
			ioController.startWatching(songsFileName, () -> {
				LOG.info("change in file {} detected", songsFileName);
				
				int selected = JOptionPane.showConfirmDialog(mainWindow, """
					The songs file on disk was changed by another process (maybe remote synchronisation). Should it be reloaded?
					
					Songs already added to the 'Present Songs' tab will remain unchanged. Unsaved changes in the 'Edit Song' tab will be lost.""",
					"Songs file changed on disk", JOptionPane.YES_NO_OPTION);
				if (selected == JOptionPane.YES_OPTION) {
					LOG.info("reloading songs from {}", songsFileName);
					loadSongs(null);
					mainWindow.reloadModels(getSongs(), getSettings());
				}
				
			});
		} catch (Exception e) {
			LOG.warn("could not start watching the songs file", e);
		}
	}
	
	public void exportStatisticsAll(File targetExcelFile) {
		statisticsController.exportStatisticsAll(new SongsModel(songs), targetExcelFile);
	}
	
	public void loadSettings() {
		LOG.debug("loading settings from file {}", FileAndDirectoryLocations.getSettingsFileName());
		settings = ioController.readSettings(is -> XMLConverter.fromXMLToPersistable(is));
		if (settings == null) {
			// there was a problem while reading
			SwingUtilities.invokeLater(() -> ErrorDialog.openDialogBlocking(mainWindow, """
				Could not read saved settings!
				
				Started with default values,
				you might have to adjust some settings
				(see tab "Global Settings")."""));
			settings = new SettingsModel();
		}
		loadDefaultSettingsForUnsetSettings();
	}
	
	private void loadDefaultSettingsForUnsetSettings() {
		putDefaultIfKeyIsUnset(SettingKey.BACKGROUND_COLOR, Color.BLACK);
		putDefaultIfKeyIsUnset(SettingKey.TEXT_COLOR, Color.WHITE);
		putDefaultIfKeyIsUnset(SettingKey.BACKGROUND_COLOR_2, settings.get(SettingKey.BACKGROUND_COLOR, Color.class));
		putDefaultIfKeyIsUnset(SettingKey.TEXT_COLOR_2, settings.get(SettingKey.TEXT_COLOR, Color.class));
		
		putDefaultIfKeyIsUnset(SettingKey.TOP_MARGIN, Integer.valueOf(10));
		putDefaultIfKeyIsUnset(SettingKey.LEFT_MARGIN, Integer.valueOf(0));
		putDefaultIfKeyIsUnset(SettingKey.RIGHT_MARGIN, Integer.valueOf(0));
		putDefaultIfKeyIsUnset(SettingKey.BOTTOM_MARGIN, Integer.valueOf(20));
		putDefaultIfKeyIsUnset(SettingKey.DISTANCE_TITLE_TEXT, Integer.valueOf(20));
		putDefaultIfKeyIsUnset(SettingKey.DISTANCE_TEXT_COPYRIGHT, Integer.valueOf(20));
		
		putDefaultIfKeyIsUnset(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.TITLE_AND_LYRICS);
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.ONLY_LYRICS);
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.LYRICS_AND_CHORDS_AND_CHORD_SEQUENCE);
		List<SelectableDisplay> availableScreens = ScreenHelper.getScreens();
		if (availableScreens.size() > 1) {
			putDefaultIfKeyIsUnset(SettingKey.SCREEN_1_DISPLAY, Integer.valueOf(availableScreens.get(1).getIndex()));
		} else {
			putDefaultIfKeyIsUnset(SettingKey.SCREEN_1_DISPLAY, null);
		}
		putDefaultIfKeyIsUnset(SettingKey.SCREEN_2_DISPLAY, null);
		putDefaultIfKeyIsUnset(SettingKey.MINIMAL_SCROLLING, Boolean.FALSE);
		putDefaultIfKeyIsUnset(SettingKey.MINIMAL_SCROLLING_2, Boolean.FALSE);
		
		putDefaultIfKeyIsUnset(SettingKey.SHOW_TITLE, Boolean.TRUE);
		putDefaultIfKeyIsUnset(SettingKey.TITLE_FONT, new Font(Font.SERIF, Font.BOLD, 20));
		putDefaultIfKeyIsUnset(SettingKey.LYRICS_FONT, new Font(Font.SERIF, Font.PLAIN, 20));
		putDefaultIfKeyIsUnset(SettingKey.TRANSLATION_FONT, new Font(Font.SERIF, Font.PLAIN, 20));
		putDefaultIfKeyIsUnset(SettingKey.COPYRIGHT_FONT, new Font(Font.SERIF, Font.ITALIC, 20));
		putDefaultIfKeyIsUnset(SettingKey.CHORD_SEQUENCE_FONT, new Font(Font.SERIF, Font.ITALIC, 20));
		putDefaultIfKeyIsUnset(SettingKey.TITLE_FONT_2, settings.get(SettingKey.TITLE_FONT, Font.class));
		putDefaultIfKeyIsUnset(SettingKey.LYRICS_FONT_2, settings.get(SettingKey.LYRICS_FONT, Font.class));
		putDefaultIfKeyIsUnset(SettingKey.TRANSLATION_FONT_2, settings.get(SettingKey.TRANSLATION_FONT, Font.class));
		putDefaultIfKeyIsUnset(SettingKey.COPYRIGHT_FONT_2, settings.get(SettingKey.COPYRIGHT_FONT, Font.class));
		putDefaultIfKeyIsUnset(SettingKey.CHORD_SEQUENCE_FONT_2, settings.get(SettingKey.CHORD_SEQUENCE_FONT, Font.class));
		putDefaultIfKeyIsUnset(SettingKey.LOGO_FILE, "");
		putDefaultIfKeyIsUnset(SettingKey.SECONDS_UNTIL_COUNTED, Integer.valueOf(60));
		
		putDefaultIfKeyIsUnset(SettingKey.SLIDE_SHOW_DIRECTORY, null);
		putDefaultIfKeyIsUnset(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, Integer.valueOf(20));
		putDefaultIfKeyIsUnset(SettingKey.FADE_TIME, Integer.valueOf(300));
		
		putDefaultIfKeyIsUnset(SettingKey.CALENDAR_URL, "");
		putDefaultIfKeyIsUnset(SettingKey.CALENDAR_DAYS_AHEAD, Integer.valueOf(7));
		
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_ENABLED, false);
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_PASSWORD, "");
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_SERVER, "tcp://localhost:1883");
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_USERNAME, "");
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_PREFIX, "");
		putDefaultIfKeyIsUnset(SettingKey.REMOTE_NAMESPACE, "default");
		
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
	
	public synchronized boolean saveSettings() {
		// TODO move method to IOController !?
		File file = new File(FileAndDirectoryLocations.getSettingsFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromPersistableToXML(settings, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write settings to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	private SongsModel populateSongsModel(String fileName) {
		LOG.debug("loading songs from file {}", fileName);
		SongsModel songsModel = ioController.readSongs(fileName, is -> XMLConverter.fromXMLToPersistable(is));
		if (songsModel != null) {
			return songsModel;
		} else {
			String fileNameUsed = FileAndDirectoryLocations.getSongsFileName(fileName);
			LOG.error("could not load songs from {}", fileNameUsed);
			ErrorDialog.openDialogBlocking(null, "Could not load songs from file:\n" + fileNameUsed
				+ "\n\nThis is a fatal error, exiting.\nSee log file for more details.");
			shutdown(-1);
			return null;
		}
	}
	
	public synchronized boolean saveSongs() {
		// TODO move method to IOController !?
		File songsBackupFile = saveSongsToBackupFile();
		if (songsBackupFile == null) {
			LOG.error("could not write backup file while saving database");
			ErrorDialog.openDialog(null, """
				Could not save songs!
				
				(Phase 1 - write backup file)""");
			return false;
		}
		try {
			ioController.stopWatching();
			Path source = songsBackupFile.toPath();
			Path target = Paths.get(FileAndDirectoryLocations.getSongsFileName(songsFileName));
			LOG.info("copying {} to {}", source, target);
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOG.error("could not copy backup to real file while saving database");
			ErrorDialog.openDialog(null, """
				Could not save songs!
				
				(Phase 2 - write file)""");
			return false;
		} finally {
			ioController.startWatchingAgain();
		}
		
		manageOldBackups();
		
		return true;
	}
	
	private File saveSongsToBackupFile() {
		// TODO move method to IOController !?
		File file = new File(FileAndDirectoryLocations.getSongsBackupFile());
		try (OutputStream xmlOutputStream = new FileOutputStream(file)) {
			LOG.debug("writing songs to backup file \"{}\"", file.getAbsolutePath());
			XMLConverter.fromPersistableToXML(new SongsModel(songs), xmlOutputStream);
			return file;
		} catch (IOException e) {
			LOG.error("could not write songs to backup file \"" + file.getAbsolutePath() + "\"", e);
			return null;
		}
	}
	
	/**
	 * delete backup files older than 21 days, but retain 30 backups at least
	 */
	private void manageOldBackups() {
		// TODO move method to IOController !?
		try {
			Files.list(Paths.get(FileAndDirectoryLocations.getSongsBackupDir()))
				// ordering: firstly by modification date DESC, secondly by file name DESC
				.sorted((p1, p2) -> ComparisonChain.start()
					.compare(lastModified(p1), lastModified(p2), Ordering.natural().reversed())
					.compare(p1, p2, Ordering.natural().reversed())
					.result())
				.skip(30)
				.filter(p -> lastModified(p).isBefore(LocalDateTime.now().minusDays(21)))
				.forEach(p -> {
					LOG.info("deleting old backup {}", p);
					delete(p);
				});
		} catch (Exception e) {
			LOG.warn("error while managing backup files");
		}
	}
	
	private LocalDateTime lastModified(Path path) {
		try {
			return Instant.ofEpochMilli(Files.getLastModifiedTime(path).toMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void delete(Path p) {
		try {
			Files.delete(p);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SongsModel getSongs() {
		return songs;
	}
	
	public SettingsModel getSettings() {
		return settings;
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
	
	public static void initAnimationTimer() {
		final TimingSource animationTimer = new SwingTimerTimingSource(5, TimeUnit.MILLISECONDS);
		Animator.setDefaultTimingSource(animationTimer);
		animationTimer.init();
	}
	
	public void startSlideShowCycle(final int seconds) {
		Runnable slideShowRunnable = () -> {
			LOG.debug("start sleeping for {} seconds (slide show)", seconds);
			try {
				if (slideShowImages == null || !slideShowImages.hasNext()) {
					LOG.info("no image files available for slide show");
					return;
				}
				showSlide(slideShowImages.next());
				Thread.sleep(seconds * 1000);
				startSlideShowCycle(seconds);
			} catch (InterruptedException e) {
				// if interrupted, do nothing (the slide show was stopped)
				LOG.trace("interrupted (slide show)");
			}
		};
		if (executor.isShutdown()) {
			throw new IllegalStateException("background executor is stopped");
		} else {
			slideShowFuture = executor.submit(slideShowRunnable);
		}
	}
	
	public String loadLogo() {
		String logoPath = settings.get(SettingKey.LOGO_FILE, String.class);
		if (logoPath != null && !logoPath.equals("")) {
			File logoFile = new File(logoPath);
			return logoFile.getAbsolutePath();
		}
		return null;
	}
	
	private void showSlide(File imageFile) {
		present(new Presentable(null, imageFile.getAbsolutePath()));
	}
	
	public void stopSlideShow() {
		if (slideShowFuture != null) {
			LOG.debug("stopping slide show");
			slideShowFuture.cancel(true);
			slideShowFuture = null;
			slideShowImages = null;
		} else {
			LOG.trace("wanted to stop slide show, but nothing to do");
		}
	}
	
	public boolean presentSlideShow() {
		Path imageDirectory = Paths.get(settings.get(SettingKey.SLIDE_SHOW_DIRECTORY, String.class));
		if (imageDirectory == null || !Files.exists(imageDirectory) || !Files.isReadable(imageDirectory) || !Files.isDirectory(imageDirectory)) {
			LOG.warn("directory {} could not be opened", imageDirectory);
			return false;
		}
		
		slideShowImages = getImageIterator(imageDirectory);
		if (slideShowImages == null) {
			LOG.warn("images could not be loaded from directory {}", imageDirectory.toString());
			return false;
		}
		
		Integer seconds = settings.get(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, Integer.class);
		if (seconds == null) {
			LOG.warn("slide show settings: seconds until next picture missing");
			return false;
		}
		
		// stop the "old" slide show if it is still running
		stopSlideShow();
		
		startSlideShowCycle(seconds);
		return true;
	}
	
	public boolean presentCalendar() {
		String url = settings.get(SettingKey.CALENDAR_URL, String.class);
		if (StringUtils.isBlank(url)) {
			LOG.warn("calendar URL is unset");
			ErrorDialog.openDialog(null, """
				The calendar URL is not set!
				
				Please correct that on the tab "Global Settings".""");
			return false;
		}
		Integer daysAhead = settings.get(SettingKey.CALENDAR_DAYS_AHEAD, Integer.class);
		if (daysAhead == null || daysAhead < 0) {
			LOG.warn("illegal value for calendar days ahead: {}", daysAhead);
			ErrorDialog.openDialog(null, """
				The value for "days ahead" is illegal. It has to be a number equal to or greater than 0.
				
				Please correct that on the tab "Global Settings".""");
			return false;
		}
		Song iCalSong = null;
		try {
			ICalInterpreter iCalInterpreter = new ICalInterpreter(url, daysAhead, getConfiguredLocale());
			// if downloading and interpreting the iCal takes too long, maybe move the loading to startup phase
			iCalSong = iCalInterpreter.getInterpretedData();
			if (iCalSong == null) {
				LOG.warn("calendar data could not be interpreted");
				ErrorDialog.openDialog(null, "The calendar data could not be interpreted.");
				return false;
			}
		} catch (Exception e) {
			LOG.warn("problem while interpreting calendar", e);
			ErrorDialog.openDialog(null, "problem while interpreting calendar:\n\n" + e.getMessage());
			return false;
		}
		
		mainWindow.present(iCalSong);
		return true;
	}
	
	private Locale getConfiguredLocale() {
		Options options = Options.getInstance();
		if (options.getLanguage() != null && options.getCountry() == null) {
			return Locale.forLanguageTag(options.getLanguage());
		} else if (options.getLanguage() != null && options.getCountry() != null) {
			return Locale.forLanguageTag(options.getLanguage() + "-" + options.getCountry());
		} else {
			return Locale.getDefault();
		}
	}
	
	private Iterator<File> getImageIterator(Path imageDirectory) {
		try {
			List<File> images = Files.list(imageDirectory)
				.map(path -> path.toFile())
				.filter(file -> imagePattern.matcher(file.getName()).matches())
				.sorted()
				.toList();
			return Iterables.cycle(images).iterator();
		} catch (IOException e) {
			return null;
		}
	}
	
	public RemoteController getRemoteController() {
		return remoteController;
	}
	
	public SongsModelController getSongsController() {
		return songsController;
	}
	
}
