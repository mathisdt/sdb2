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

import org.apache.commons.text.WordUtils;
import org.zephyrsoft.sdb2.service.FieldName;

/**
 * Determines on which data the song list filter should operate.
 */
public enum FilterTypeEnum {
	ONLY_TITLE(FieldName.TITLE), TITLE_AND_LYRICS(FieldName.TITLE, FieldName.LYRICS), ONLY_LYRICS(FieldName.LYRICS);
	
	private FieldName[] fields;
	
	private FilterTypeEnum(FieldName... fields) {
		this.fields = fields;
	}
	
	public FieldName[] getFields() {
		return fields;
	}
	
	public String getInternalName() {
		return WordUtils.capitalizeFully(name(), new char[] { '_' }).replaceAll("_", "");
	}
	
	public static FilterTypeEnum withInternalName(String internalName) {
		for (FilterTypeEnum fte : values()) {
			if (fte.getInternalName().equalsIgnoreCase(internalName)) {
				return fte;
			}
		}
		return null;
	}
}
