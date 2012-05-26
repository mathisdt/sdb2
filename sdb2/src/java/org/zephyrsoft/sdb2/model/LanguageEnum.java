package org.zephyrsoft.sdb2.model;

import org.zephyrsoft.util.StringTools;

/**
 * Determines the language of a song.
 * 
 * @author Mathis Dirksen-Thedens
 */
public enum LanguageEnum {
	GERMAN("de"), ENGLISH("en"), MIXED("mix");
	
	private String languageAbbreviation;
	
	private LanguageEnum(String languageAbbreviation) {
		this.languageAbbreviation = languageAbbreviation;
	}
	
	public String getLanguageAbbreviation() {
		return languageAbbreviation;
	}
	
	public static LanguageEnum getByLanguageAbbreviation(String languageAbbreviation) {
		for (LanguageEnum value : values()) {
			if (StringTools.equals(value.getLanguageAbbreviation(), languageAbbreviation)) {
				return value;
			}
		}
		return null;
	}
}
