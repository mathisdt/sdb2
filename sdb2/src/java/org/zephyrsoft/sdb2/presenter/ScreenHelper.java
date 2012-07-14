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
package org.zephyrsoft.sdb2.presenter;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.zephyrsoft.util.StringTools;

/**
 * Helper class for dealing with multiple screens.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ScreenHelper {
	
	private ScreenHelper() {
		// this class should not be instantiated
	}
	
	/**
	 * Get the comprehensive list of all screens attached to the system.
	 */
	public static List<GraphicsDevice> getScreens() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return Collections.unmodifiableList(Arrays.asList(env.getScreenDevices()));
	}
	
	/**
	 * Get a screen from the given list that has the specified ID.
	 * 
	 * @param devices list of possible screens
	 * @param screenId the ID to find
	 */
	public static GraphicsDevice getScreen(List<GraphicsDevice> devices, String screenId) {
		for (GraphicsDevice device : devices) {
			if (StringTools.equals(getScreenId(device), screenId)) {
				return device;
			}
		}
		return null;
	}
	
	/**
	 * Get the ID of a screen.
	 */
	public static String getScreenId(GraphicsDevice screen) {
		if (screen == null) {
			return null;
		} else {
			return screen.getIDstring();
		}
	}
	
}
