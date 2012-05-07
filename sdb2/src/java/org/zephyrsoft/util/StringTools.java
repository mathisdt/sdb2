package org.zephyrsoft.util;

/**
 * Methods which are missing in the {@link String} class.<br/>
 * <br/>
 * <b>All arguments have to be non-{@code null}!</b>
 * 
 * @author Mathis Dirksen-Thedens
 */
public class StringTools {
	
	/**
	 * Compare two Strings with special care. If one of the Strings is {@code null}, it is assumed "smaller" than the
	 * non-null String.
	 * 
	 * @see String#compareTo(String)
	 * 
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
}
