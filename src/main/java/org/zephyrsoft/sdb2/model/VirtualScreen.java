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

import java.awt.Font;

import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;

/**
 * Screen 1 or Screen 2, each is configurable separately under settings.
 *
 * Has an impact on the fonts to use for presentation.
 */
public enum VirtualScreen {
	SCREEN_1, SCREEN_2;
	
	public Font getTitleFont(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.TITLE_FONT, Font.class)
			: settings.get(SettingKey.TITLE_FONT_2, Font.class);
	}
	
	public Font getLyricsFont(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.LYRICS_FONT, Font.class)
			: settings.get(SettingKey.LYRICS_FONT_2, Font.class);
	}
	
	public Font getChordSequenceFont(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.CHORD_SEQUENCE_FONT, Font.class)
			: settings.get(SettingKey.CHORD_SEQUENCE_FONT_2, Font.class);
	}
	
	public Font getTranslationFont(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.TRANSLATION_FONT, Font.class)
			: settings.get(SettingKey.TRANSLATION_FONT_2, Font.class);
	}
	
	public Font getCopyrightFont(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.COPYRIGHT_FONT, Font.class)
			: settings.get(SettingKey.COPYRIGHT_FONT_2, Font.class);
	}
}