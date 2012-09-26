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

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Defines the logging directory for use in Logback.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class LogDirectoryDefiner extends PropertyDefinerBase {
	
	@Override
	public String getPropertyValue() {
		return FileAndDirectoryLocations.getLogDir();
	}
	
}
