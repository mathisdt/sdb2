package org.zephyrsoft.sdb2.model;

import org.zephyrsoft.util.StringTools;

/**
 * Types of presentation screen contents.
 * 
 * @author Mathis Dirksen-Thedens
 */
public enum ScreenContentsEnum {
	ONLY_LYRICS("lyrics"), LYRICS_AND_CHORDS("lyrics+chords");
	
	private String abbreviation;
	
	private ScreenContentsEnum(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public static ScreenContentsEnum getByAbbreviation(String abbreviation) {
		for (ScreenContentsEnum value : values()) {
			if (StringTools.equals(value.getAbbreviation(), abbreviation)) {
				return value;
			}
		}
		return null;
	}
}
