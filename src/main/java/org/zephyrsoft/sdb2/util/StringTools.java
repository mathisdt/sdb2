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
package org.zephyrsoft.sdb2.util;

import java.text.Collator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Methods which are missing in the {@link String} class.<br/>
 * <br/>
 * <b>All arguments have to be non-{@code null}!</b>
 */
public final class StringTools {
	
	private StringTools() {
		// this class is not intended for instantiation
	}
	
	/**
	 * Create a random UUID value.
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Compare two Strings with special care. If one of the Strings is {@code null}, it is assumed "smaller" than the
	 * non-null String.
	 *
	 * @see String#compareTo(String)
	 * @param one
	 *            first parameter to compare
	 * @param two
	 *            second parameter to compare
	 * @return the same scheme as in {@link String#compareTo(String)}
	 */
	public static int compareWithNullFirst(String one, String two) {
		if (one == null && two != null) {
			return -1;
		} else if (one != null && two == null) {
			return 1;
		} else if (one == null && two == null) {
			return 0;
		} else if (one != null && two != null) {
			return one.compareTo(two);
		} else {
			// to silence the Eclipse warning, will never be executed:
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Compare two Strings with regards to the current default locale. If one of the Strings is {@code null}, it is
	 * assumed "smaller" than the non-null String.
	 *
	 * @see String#compareTo(String)
	 * @param one
	 *            first parameter to compare
	 * @param two
	 *            second parameter to compare
	 * @return the same scheme as in {@link String#compareTo(String)}
	 */
	public static int compareLocaleBasedWithNullFirst(String one, String two) {
		if (one == null && two != null) {
			return -1;
		} else if (one != null && two == null) {
			return 1;
		} else if (one == null && two == null) {
			return 0;
		} else if (one != null && two != null) {
			Collator collator = Collator.getInstance();
			return collator.compare(one, two);
		} else {
			// to silence the Eclipse warning, will never be executed:
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Equals method capable of handling null values.
	 *
	 * @param one
	 *            first string
	 * @param two
	 *            second string
	 * @return {@code true} if both strings are {@code null} or if they are equal via {@link String#equals(Object)}
	 *         method, {@code false} else
	 */
	public static boolean equals(String one, String two) {
		return (one == null && two == null) || (one != null && one.equals(two));
	}
	
	/**
	 * Equals method capable of handling null values. {@code null} values are treated as equal to empty strings.
	 *
	 * @param one
	 *            first string
	 * @param two
	 *            second string
	 * @return {@code true} if both strings are ({@code null} or an empty string) or if they are equal via
	 *         {@link String#equals(Object)} method, {@code false} else
	 */
	public static boolean equalsWithNullAsEmpty(String one, String two) {
		return (StringUtils.isEmpty(one) && StringUtils.isEmpty(two)) || (one != null && one.equals(two));
	}
	
	/**
	 * Tests if a string is null or empty.
	 *
	 * @param toTest
	 *            the string to test
	 * @return {@code true} if the string is null or empty, {@code false} else
	 */
	public static boolean isEmpty(String toTest) {
		return toTest == null || toTest.isEmpty();
	}
	
	/**
	 * Returns exactly the argument, unless the argument is {@code null}. In this case, this method returns the empty
	 * string.
	 *
	 * @param in
	 *            the string to check
	 * @return a non-null value
	 */
	public static String nullAsEmptyString(String in) {
		if (in == null) {
			return "";
		} else {
			return in;
		}
	}
	
	/**
	 * Tests is a string is null, empty or only contains whitespace.
	 *
	 * @param in
	 *            the string to test
	 * @return {@code true} if the string is null, empty or only contains whitespace; {@code false} else
	 */
	public static boolean isBlank(String in) {
		return isEmpty(in) || in.trim().length() == 0;
	}
}
