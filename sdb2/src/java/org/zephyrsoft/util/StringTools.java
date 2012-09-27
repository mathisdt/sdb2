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
package org.zephyrsoft.util;

import java.text.Collator;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Methods which are missing in the {@link String} class.<br/>
 * <br/>
 * <b>All arguments have to be non-{@code null}!</b>
 * 
 * @author Mathis Dirksen-Thedens
 */
public class StringTools {
	
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
	 * Convert a string to an easily comparable form, especially remove all punctuation and newline characters.
	 * 
	 * @param in the input string
	 * @return the easily comparable form
	 */
	public static String toEasilyComparable(String in) {
		if (in == null) {
			return null;
		} else {
			return in.replaceAll("\\W", " ").replaceAll("  ", " ").toLowerCase();
		}
	}
	
	/**
	 * Convert a string which is in camel case to all caps, separated with underscores. Example:
	 * "camelCaseStringExample" => "CAMEL_CASE_STRING_EXAMPLE"
	 * 
	 * @param camelCasedString the camel-cased string
	 * @return the string with underscores in all caps
	 */
	public static String camelCaseToUnderscored(String camelCasedString) {
		if (camelCasedString == null || camelCasedString.length() == 0) {
			return "";
		} else {
			return camelCasedString.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
				.replaceAll("([a-z\\d])([A-Z])", "$1_$2").toUpperCase(Locale.ENGLISH);
		}
	}
	
	/**
	 * Convert a string which is separated with underscores to camel case. Example: "UNDERSCORED_STRING_EXAMPLE" =>
	 * "underscoredStringExample"
	 * 
	 * @param underscoredString the underscore-separated string
	 * @return the camel-cased string
	 */
	public static String underscoredToCamelCase(String underscoredString) {
		if (underscoredString == null || underscoredString.length() == 0) {
			return "";
		} else {
			String ret = WordUtils.capitalizeFully(underscoredString, new char[] {'_'}).replaceAll("_", "");
			ret = ret.substring(0, 1).toLowerCase(Locale.ENGLISH) + ret.substring(1);
			return ret;
		}
	}
	
	/**
	 * Compare two Strings with special care. If one of the Strings is {@code null}, it is assumed "smaller" than the
	 * non-null String.
	 * 
	 * @see String#compareTo(String)
	 * @param one first parameter to compare
	 * @param two second parameter to compare
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
	 * @param one first parameter to compare
	 * @param two second parameter to compare
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
	 * A sibling of {@link String#replaceAll(String, String)} but without regex interpretation.
	 * 
	 * @param in the String in which the replacement shall take place
	 * @param toreplace what is to be replaced
	 * @param replacewith the replacement
	 * @return the input in which every occurrence of {@code toreplace} was replaced with {@code replacewith}
	 */
	public static String replace(String in, String toreplace, String replacewith) {
		String ret = in;
		while (ret.indexOf(toreplace) >= 0) {
			ret =
				ret.substring(0, ret.indexOf(toreplace)) + replacewith
					+ ret.substring(ret.indexOf(toreplace) + toreplace.length());
		}
		return ret;
	}
	
	/**
	 * Makes sure that a {@link String} does have a specific number of characters at maximum.
	 * 
	 * @param in the input
	 * @param maxlength the maximum allowed length
	 * @return the input, cut down to
	 */
	public static String cutdown(String in, int maxlength) {
		if (in.length() <= maxlength) {
			return in;
		} else {
			return in.substring(0, maxlength - 3) + "...";
		}
	}
	
	/**
	 * Repeat a {@link String}.
	 * 
	 * @param torepeat the String which is to be repeated
	 * @param count how many times should it be repeated
	 * @return concatenation
	 */
	public static String repeat(String torepeat, int count) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < count; i++) {
			ret.append(torepeat);
		}
		return ret.toString();
	}
	
	/**
	 * Trim only the right side of a {@link String}.
	 * 
	 * @param in the input
	 * @return the input which was right-trimmed (any spaces to the left are still intact)
	 */
	public static String rightTrim(String in) {
		return "X".concat(in).trim().substring(1);
	}
	
	/**
	 * A sibling of the {@link String#contains(CharSequence)} method, but ignorant of casing.
	 * 
	 * @param haystack the String in which to search
	 * @param needle the String which to find
	 * @return {@code true} only if {@code haystack} contains {@code needle} (ignoring the casing of both)
	 */
	public static boolean containsIgnoreCase(String haystack, String needle) {
		return haystack.toLowerCase().contains(needle.toLowerCase());
	}
	
	/**
	 * Equals method capable of handling null values.
	 * 
	 * @param one first string
	 * @param two second string
	 * @return {@code true} if both strings are {@code null} or if they are equal via {@link String#equals(Object)}
	 *         method, {@code false} else
	 */
	public static boolean equals(String one, String two) {
		return (one == null && two == null) || (one != null && one.equals(two));
	}
	
	/**
	 * Tests is a string is null or empty.
	 * 
	 * @param in the string to test
	 * @return {@code true} if the string is null or empty, {@code false} else
	 */
	public static boolean isEmpty(String in) {
		return in == null || in.isEmpty();
	}
	
	/**
	 * Returns exactly the argument, unless the argument is {@code null}. In this case, this method returns the empty
	 * string.
	 * 
	 * @param in the string to check
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
	 * @param in the string to test
	 * @return {@code true} if the string is null, empty or only contains whitespace; {@code false} else
	 */
	public static boolean isBlank(String in) {
		return isEmpty(in) || in.trim().length() == 0;
	}
}
