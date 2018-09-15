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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.zephyrsoft.sdb2.gui.KeyboardShortcutManager;
import org.zephyrsoft.sdb2.gui.MainWindow;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.service.IndexerService;
import org.zephyrsoft.sdb2.service.IndexerServiceImpl;

/**
 * Configures the DI context.
 * 
 * @author Mathis Dirksen-Thedens
 */
@Configuration
public class SpringConfiguration {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpringConfiguration.class);
	
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
		LOG.debug("feature manager built");
		
		return featureManager;
	}
	
	@Bean
	public IOController ioController() {
		return new IOController();
	}
	
	@Bean
	public StatisticsController statisticsController() {
		StatisticsController statisticsController = new StatisticsController(ioController());
		statisticsController.loadStatistics();
		return statisticsController;
	}
	
	@Bean
	public MainController mainController() {
		MainController mainController = new MainController(ioController(), statisticsController());
		mainController.setupLookAndFeel();
		mainController.loadSettings();
		mainController.loadSongs(Options.getInstance().getSongsFile());
		return mainController;
	}
	
	@Bean
	public KeyboardShortcutManager keyboardShortcutManager() {
		return new KeyboardShortcutManager();
	}
	
	@Bean
	public IndexerService<Song> indexerService() {
		return new IndexerServiceImpl();
	}
	
	@Bean
	public MainWindow mainWindow() {
		return new MainWindow(mainController(), keyboardShortcutManager(), indexerService());
	}
	
}
