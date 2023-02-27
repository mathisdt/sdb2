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
package org.zephyrsoft.sdb2.util;

import org.zephyrsoft.sdb2.FileAndDirectoryLocations;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;

/**
 * Provides the rollover directory which can be changed through the "--logs-rollover ..." command line parameter.
 *
 * It's important that Logback is initialized *after* parsing the arguments into the Options instance,
 * which typically is done by fetching the first logger from {@code LoggerFactory}.
 */
public class LogbackRolloverDirectoryProvider implements PropertyDefiner {
	
	@Override
	public void setContext(Context context) {
		// do nothing
	}
	
	@Override
	public Context getContext() {
		return null;
	}
	
	@Override
	public void addStatus(Status status) {
		// do nothing
	}
	
	@Override
	public void addInfo(String msg) {
		// do nothing
	}
	
	@Override
	public void addInfo(String msg, Throwable ex) {
		// do nothing
	}
	
	@Override
	public void addWarn(String msg) {
		// do nothing
	}
	
	@Override
	public void addWarn(String msg, Throwable ex) {
		// do nothing
	}
	
	@Override
	public void addError(String msg) {
		// do nothing
	}
	
	@Override
	public void addError(String msg, Throwable ex) {
		// do nothing
	}
	
	@Override
	public String getPropertyValue() {
		return FileAndDirectoryLocations.getLogRolloverDir();
	}
}
