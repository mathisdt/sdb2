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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.zephyrsoft.sdb2.api.Endpoints;
import org.zephyrsoft.sdb2.gui.KeyboardShortcutManager;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.service.ExportService;
import org.zephyrsoft.sdb2.service.IndexerService;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

/**
 * Configures the DI context.
 */
@Configuration
@EnableWebMvc
public class SpringConfiguration implements WebApplicationInitializer {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpringConfiguration.class);

	@Override
	public void onStartup(final ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SpringConfiguration.class);

		servletContext.addListener(new ContextLoaderListener(rootContext));

		AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
	}

	@Bean
	public Endpoints endpoints() {
		return new Endpoints();
	}

	@Bean
	public FeatureManager featureManager() {
		FeatureManager featureManager = new FeatureManagerBuilder()
			.featureEnum(Feature.class)
			.stateRepository(new InMemoryStateRepository())
			.userProvider(new NoOpUserProvider())
			.build();
		
		// enable experimental features only if "-Dexperimental=true" was set on start
		boolean enabled = System.getProperty("experimental") != null
			&& System.getProperty("experimental").equalsIgnoreCase("true");
		
		featureManager.setFeatureState(new FeatureState(Feature.HIGHLIGHT_FILTER_MATCHES, enabled));
		
		StaticFeatureManagerProvider.setFeatureManager(featureManager);
		LOG.trace("feature manager built");
		
		return featureManager;
	}
	
	@Bean
	public IOController ioController() {
		return new IOController();
	}
	
	@Bean
	public StatisticsController statisticsController() {
		StatisticsController statisticsController = new StatisticsController(ioController());
		try {
			statisticsController.loadStatistics();
		} catch (Exception e) {
			ErrorDialog.openDialogBlocking(null, "Error while loading statistics! Please check the file:\n"
				+ FileAndDirectoryLocations.getStatisticsFileName()
				+ "\n\nIf you can't fix the file, please delete it, but be warned:\n"
				+ "your statistics up to now will be gone!");
			throw e;
		}
		return statisticsController;
	}
	
	@Bean
	public MainController mainController() {
		MainController mainController = new MainController(ioController(), statisticsController());
		mainController.setupLookAndFeel();
		try {
			mainController.loadSettings();
		} catch (Exception e) {
			ErrorDialog.openDialogBlocking(null, "Error while loading settings! Please check the file:\n"
				+ FileAndDirectoryLocations.getSettingsFileName()
				+ "\n\nIf you can't fix the file, please delete it, but be warned:\n"
				+ "your settings (e.g. colors and fonts) will be reset\n"
				+ "to default values!");
			throw e;
		}
		try {
			mainController.loadSongs(Options.getInstance().getSongsFile());
			mainController.startWatchingSongsFile();
		} catch (Exception e) {
			String songsFile = FileAndDirectoryLocations.getSongsFileName(Options.getInstance().getSongsFile());
			ErrorDialog.openDialogBlocking(null, "Error while loading songs! Please check the file:\n"
				+ songsFile
				+ "\n\nIf you can't fix the file:\n\n"
				+ "1. check if a recent backup from the directory\n   "
				+ FileAndDirectoryLocations.getSongsBackupDir()
				+ "\n   can be used instead (copy the file over to\n   "
				+ songsFile
				+ "\n   and restart the Song Database)\n\n"
				+ "2. as a last resource, delete \n   "
				+ songsFile
				+ "\n   But be warned: all your songs will be gone!");
			throw e;
		}
		try {
			mainController.exportStatisticsIfNecessary();
		} catch (Exception e) {
			ErrorDialog.openDialogBlocking(null, "Could not export the statistics!\n\nThis is not fatal, but you should have a look at it.");
			LOG.warn("", e);
		}
		// Run this in headless mode:
		// mainController.initRemoteController();
		return mainController;
	}
	
	@Bean
	public KeyboardShortcutManager keyboardShortcutManager() {
		return new KeyboardShortcutManager();
	}
	
	@Bean
	public IndexerService indexerService() {
		return new IndexerService();
	}
	
	@Bean
	public ExportService exportService() {
		try {
			return new ExportService();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Bean
	public MainWindow mainWindow(MainController mainController) {
		MainWindow mainWindow = new MainWindow(mainController, keyboardShortcutManager(), indexerService(), exportService());
		// Init remote control after mainwindow is ready:
		mainController.initRemoteController();
		return mainWindow;
	}
	
}
