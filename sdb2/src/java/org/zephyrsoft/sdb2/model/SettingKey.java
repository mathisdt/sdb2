package org.zephyrsoft.sdb2.model;

import java.awt.Color;
import java.awt.Font;

/**
 * The keys to use with the {@link SettingsModel}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public enum SettingKey {
	TITLE_FONT(Font.class),
	LYRICS_FONT(Font.class),
	TRANSLATION_FONT(Font.class),
	COPYRIGHT_FONT(Font.class),
	TEXT_COLOR(Color.class),
	BACKGROUND_COLOR(Color.class),
	LOGO_FILE(String.class),
	TOP_MARGIN(Integer.class),
	LEFT_MARGIN(Integer.class),
	RIGHT_MARGIN(Integer.class),
	BOTTOM_MARGIN(Integer.class),
	DISTANCE_TITLE_TEXT(Integer.class),
	DISTANCE_TEXT_COPYRIGHT(Integer.class),
	SONG_LIST_FILTER(FilterTypeEnum.class),
	SCREEN_1_DISPLAY(String.class),
	SCREEN_1_CONTENTS(ScreenContentsEnum.class),
	SCREEN_2_DISPLAY(String.class),
	SCREEN_2_CONTENTS(ScreenContentsEnum.class),
	SECONDS_UNTIL_COUNTED(Integer.class);
	
	private Class<?> type;
	
	private SettingKey(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	}
}
