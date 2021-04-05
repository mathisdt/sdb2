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
package org.zephyrsoft.sdb2.model;

/**
 * Determines the language of a song.
 */
public enum LanguageEnum {
	GERMAN("German"), ENGLISH("English"), MIXED("Mixed");
	
	private final String description;
	
	private LanguageEnum(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getInternalName() {
		return name().toLowerCase();
	}
	
	public static LanguageEnum withInternalName(String internalName) {
		for (LanguageEnum le : values()) {
			if (le.getInternalName().equalsIgnoreCase(internalName)) {
				return le;
			}
		}
		return null;
	}
}
