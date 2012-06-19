package org.zephyrsoft.sdb2.presenter;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
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
	
	public static List<GraphicsDevice> getScreens() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return Arrays.asList(env.getScreenDevices());
	}
	
	public static GraphicsDevice getScreen(String screenId) {
		List<GraphicsDevice> devices = getScreens();
		for (GraphicsDevice device : devices) {
			if (StringTools.equals(getScreenId(device), screenId)) {
				return device;
			}
		}
		return null;
	}
	
	public static String getScreenId(GraphicsDevice screen) {
		if (screen != null) {
			return screen.getIDstring();
		} else {
			throw new IllegalArgumentException("screen is null");
		}
	}
	
}
