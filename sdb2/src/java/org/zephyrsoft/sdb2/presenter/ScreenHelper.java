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
