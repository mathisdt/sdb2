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

import java.awt.Color;
import java.awt.Font;

import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;

/**
 * Screen A or Screen B, each is configurable separately under settings.
 *
 * Has an impact on the fonts to use for presentation.
 */
public enum VirtualScreen {
	SCREEN_A("A"), SCREEN_B("B");
	
	private final String name;
	
	private VirtualScreen(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Color getTextColor(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.TEXT_COLOR, Color.class)
			: settings.get(SettingKey.TEXT_COLOR_2, Color.class);
	}
	
	public Color getBackgroundColor(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.BACKGROUND_COLOR, Color.class)
			: settings.get(SettingKey.BACKGROUND_COLOR_2, Color.class);
	}
	
	public ScreenContentsEnum getScreenContents(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.class)
			: settings.get(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.class);
	}
	
	public Boolean getMinimalScrolling(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.MINIMAL_SCROLLING, Boolean.class)
			: settings.get(SettingKey.MINIMAL_SCROLLING_2, Boolean.class);
	}
	
	public Font getTitleFont(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.TITLE_FONT, Font.class)
			: settings.get(SettingKey.TITLE_FONT_2, Font.class);
	}
	
	public Font getLyricsFont(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.LYRICS_FONT, Font.class)
			: settings.get(SettingKey.LYRICS_FONT_2, Font.class);
	}
	
	public Font getChordSequenceFont(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.CHORD_SEQUENCE_FONT, Font.class)
			: settings.get(SettingKey.CHORD_SEQUENCE_FONT_2, Font.class);
	}
	
	public Font getTranslationFont(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.TRANSLATION_FONT, Font.class)
			: settings.get(SettingKey.TRANSLATION_FONT_2, Font.class);
	}
	
	public Font getCopyrightFont(SettingsModel settings) {
		return this == SCREEN_A
			? settings.get(SettingKey.COPYRIGHT_FONT, Font.class)
			: settings.get(SettingKey.COPYRIGHT_FONT_2, Font.class);
	}
}
