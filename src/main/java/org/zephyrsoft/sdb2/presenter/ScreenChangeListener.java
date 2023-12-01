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
package org.zephyrsoft.sdb2.presenter;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls (in reasonable intervals) for changes in native graphics device configuration (added screens, removed screens)
 * and notifies registered listeners when changes occur.
 */
public class ScreenChangeListener {
	
	private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual()
		.name("screen-configuration-poller-", 0)
		.factory();
	private static final Logger LOG = LoggerFactory.getLogger(ScreenChangeListener.class);
	
	private GraphicsDevice[] availableScreens;
	private final List<Runnable> listeners = new ArrayList<>();
	
	public ScreenChangeListener() {
		availableScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		
		THREAD_FACTORY.newThread(() -> {
			while (true) {
				try {
					Thread.sleep(5_000);
				} catch (InterruptedException ignored) {
					// do nothing
				}
				if (hasChanged()) {
					LOG.debug("screen configuration has changed");
					for (Runnable listener : listeners) {
						listener.run();
					}
				}
			}
		}).start();
	}
	
	private boolean hasChanged() {
		GraphicsDevice[] nowAvailableScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if (nowAvailableScreens.length != availableScreens.length) {
			availableScreens = nowAvailableScreens;
			return true;
		}
		for (int i = 0; i < nowAvailableScreens.length; i++) {
			if (!nowAvailableScreens[i].getDefaultConfiguration().getBounds().equals(
				availableScreens[i].getDefaultConfiguration().getBounds())) {
				availableScreens = nowAvailableScreens;
				return true;
			}
		}
		return false;
	}
	
	public void addListener(Runnable listener) {
		listeners.add(listener);
	}
	
}
