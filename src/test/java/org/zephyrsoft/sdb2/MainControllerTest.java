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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the behaviour of {@link MainController}.
 */
public class MainControllerTest {
	
	@Mock
	private IOController ioController;
	@Mock
	private StatisticsController statisticsController;
	private MainController mainController;
	private AutoCloseable mocks;
	
	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
		mainController = new MainController(ioController, statisticsController);
	}
	
	@Test
	public void loadSettingsForFirstTime() {
		// ioController.readSettings(...) returns null without any stubbing because it is a freshly initialized mock
		mainController.loadSettings();
		assertNotNull(mainController.getSettings());
	}
}
