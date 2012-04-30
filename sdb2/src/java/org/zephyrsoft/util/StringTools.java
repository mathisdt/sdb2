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
	 * A sibling of {@link String#replaceAll(String, String)} but without regex interpretation.
	 * 
	 * @param in the String in which the replacement shall take place
	 * @param toreplace what is to be replaced
	 * @param replacewith the replacement
	 * @return the input in which every occurrence of {@link toreplace} was replaced with {@link replacewith}
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
	 * @return {@code true} only if {@link haystack} contains {@link needle} (ignoring the casing of both)
	 */
	public static boolean containsIgnoreCase(String haystack, String needle) {
		needle = needle.toLowerCase();
		haystack = haystack.toLowerCase();
		return haystack.contains(needle);
	}
}
