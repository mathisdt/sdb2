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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple aspect to log method calls.
 *
 * @author Mathis Dirksen-Thedens
 */
public abstract aspect Trace {

	// tracing implementation:

	protected static Logger LOG = LoggerFactory.getLogger(Trace.class);

	protected static int callDepth = 0;

	protected static void traceEntry(String str) {
		callDepth++;
		logEntry(str);
	}

	protected static void traceExit(String str) {
		logExit(str);
		callDepth--;
	}

	private static void logEntry(String str) {
		String indent = getIndent();
		LOG.trace("{}--> {}", indent, str);
	}

	private static void logExit(String str) {
		String indent = getIndent();
		LOG.trace("{}<-- {}", indent, str);
	}

	private static String getIndent() {
		return StringUtils.repeat("  ", callDepth);
	}

	// AspectJ stuff:

	abstract pointcut myClass();

	pointcut myConstructor(): myClass() && execution(new(..));

	pointcut myMethod(): myClass() && execution(* *(..));

	before(): myConstructor() {
		traceEntry("" + thisJoinPointStaticPart.getSignature());
	}

	after(): myConstructor() {
		traceExit("" + thisJoinPointStaticPart.getSignature());
	}

	before(): myMethod() {
		traceEntry("" + thisJoinPointStaticPart.getSignature());
	}

	after(): myMethod() {
		traceExit("" + thisJoinPointStaticPart.getSignature());
	}

}
