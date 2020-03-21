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
package org.zephyrsoft.sdb2.model;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Matches one or more {@link SongElement}s by their respective {@link SongElementEnum}.
 */
@FunctionalInterface
public interface SongElementMatcher {
	
	boolean matches(SongElement songElement);
	
	// TODO cache all created matchers!
	
	/** Creates a matcher for {@link SongElement}s of the given type. */
	public static SongElementMatcher is(SongElementEnum value) {
		if (value == null) {
			throw new IllegalArgumentException("the given SongElementEnum cannot be null");
		}
		return songElement -> songElement != null
			&& songElement.getType() == value;
	}
	
	/** Creates a matcher for {@link SongElement}s of any other than the given type. */
	public static SongElementMatcher isNot(SongElementEnum value) {
		if (value == null) {
			throw new IllegalArgumentException("the given SongElementEnum cannot be null");
		}
		return songElement -> songElement != null
			&& songElement.getType() != value;
	}
	
	/** Creates a matcher for {@link SongElement}s of one of the given types. */
	public static SongElementMatcher isOneOf(SongElementEnum... values) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("the given SongElementEnums cannot be empty");
		}
		final List<SongElementEnum> valuesList = Lists.newArrayList(values);
		return songElement -> songElement != null
			&& valuesList.contains(songElement.getType());
	}
}
