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
package org.zephyrsoft.sdb2.remote;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Health {
	online,
	offline;
	
	private static final Logger LOG = LoggerFactory.getLogger(Health.class);
	
	public static Health valueOfBytes(byte[] bytes) {
		try {
			return Health.valueOf(new String(bytes, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.warn("Convert bytes to health failed", e);
		}
		return null;
	}
}
