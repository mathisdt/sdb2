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

import java.awt.Color;
import java.awt.Font;

import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;

/**
 * Screen 1 or Screen 2, each is configurable separately under settings.
 *
 * Has an impact on the fonts to use for presentation.
 */
public enum VirtualScreen {
	SCREEN_1(1), SCREEN_2(2);
	
	private final int number;
	
	private VirtualScreen(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
	public Color getTextColor(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.TEXT_COLOR, Color.class)
			: settings.get(SettingKey.TEXT_COLOR_2, Color.class);
	}
	
	public Color getBackgroundColor(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.BACKGROUND_COLOR, Color.class)
			: settings.get(SettingKey.BACKGROUND_COLOR_2, Color.class);
	}
	
	public ScreenContentsEnum getScreenContents(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.class)
			: settings.get(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.class);
	}
	
	public Boolean getMinimalScrolling(SettingsModel settings) {
		return this == SCREEN_1
			? settings.get(SettingKey.MINIMAL_SCROLLING, Boolean.class)
			: settings.get(SettingKey.MINIMAL_SCROLLING_2, Boolean.class);
	}
	
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
