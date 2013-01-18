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
package org.zephyrsoft.sdb2.aspects;

/**
 * Applies the tracing defined in {@link Trace} to specific classes.
 *
 * @author Mathis Dirksen-Thedens
 */
public aspect ApplyTracing extends Trace {

	// the start-up class (containing the main method) has to be excluded for Logback to init properly
	pointcut myClass():
		within(org.zephyrsoft.sdb2.MainController) ||
		within(org.zephyrsoft.sdb2.gui.*) ||
		within(org.zephyrsoft.sdb2.presenter.*) ||
		within(org.zephyrsoft.util.gui.*);

}
