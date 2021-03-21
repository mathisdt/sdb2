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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.zephyrsoft.sdb2.model.SelectableDisplay;

/**
 * Helper class for dealing with multiple screens.
 */
public final class ScreenHelper {
	
	private ScreenHelper() {
		// this class should not be instantiated
	}
	
	/**
	 * Get the comprehensive list of all screens attached to the system.
	 */
	public static List<SelectableDisplay> getScreens() {
		List<SelectableDisplay> result = new ArrayList<>();
		for (int i = 0; i < getGraphicsDevices().size(); i++) {
			result.add(new SelectableDisplay(i));
		}
		return result;
	}
	
	private static List<GraphicsDevice> getGraphicsDevices() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return Collections.unmodifiableList(Arrays.asList(env.getScreenDevices()));
	}
	
	/**
	 * Get a screen from the given list that has the specified index.
	 *
	 * @param screens
	 *            list of possible screens
	 * @param screenIndex
	 *            the index of the screen to find
	 */
	public static SelectableDisplay getScreen(List<SelectableDisplay> screens, Integer screenIndex) {
		if (screenIndex == null) {
			return null;
		}
		return getScreen(screens, screenIndex.intValue());
	}
	
	/**
	 * Get a screen from the given list that has the specified index.
	 *
	 * @param screens
	 *            list of possible screens
	 * @param screenIndex
	 *            the index of the screen to find
	 */
	public static SelectableDisplay getScreen(List<SelectableDisplay> screens, int screenIndex) {
		return screens.stream()
			.filter(scr -> scr != null && scr.getIndex() == screenIndex)
			.findFirst()
			.orElse(null);
	}
	
	public static GraphicsConfiguration getConfiguration(SelectableDisplay screen) {
		List<GraphicsDevice> graphicsDevices = getGraphicsDevices();
		return screen.getIndex() >= graphicsDevices.size()
			? null
			: graphicsDevices.get(screen.getIndex()).getDefaultConfiguration();
	}
	
}
