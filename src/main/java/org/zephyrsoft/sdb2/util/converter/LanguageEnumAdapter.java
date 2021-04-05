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
package org.zephyrsoft.sdb2.util.converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.zephyrsoft.sdb2.model.LanguageEnum;

/**
 * XML adapter for {@link LanguageEnum}.
 */
public class LanguageEnumAdapter extends XmlAdapter<String, LanguageEnum> {
	
	@Override
	public LanguageEnum unmarshal(String v) throws Exception {
		return LanguageEnum.withInternalName(v);
	}
	
	@Override
	public String marshal(LanguageEnum v) throws Exception {
		return v == null ? null : v.getInternalName();
	}
	
}
