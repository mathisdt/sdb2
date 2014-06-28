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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zephyrsoft.sdb2.gui.KeyboardShortcutManager;
import org.zephyrsoft.sdb2.gui.MainWindow;

/**
 * Configures the DI context.
 * 
 * @author Mathis Dirksen-Thedens
 */
@Configuration
public class SpringConfiguration {

	@Bean
	public StatisticsController statisticsController() {
		StatisticsController statisticsController = new StatisticsController();
		statisticsController.loadStatistics();
		return statisticsController;
	}

	@Bean
	public MainController mainController() {
		MainController mainController = new MainController(statisticsController());
		mainController.setupLookAndFeel();
		mainController.loadSettings();
		return mainController;
	}

	@Bean
	public KeyboardShortcutManager keyboardShortcutManager() {
		return new KeyboardShortcutManager();
	}

	@Bean
	public MainWindow mainWindow() {
		return new MainWindow(mainController(), keyboardShortcutManager());
	}
}
