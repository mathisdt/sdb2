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
package org.zephyrsoft.sdb2.model.settings;

import java.awt.Color;
import java.awt.Font;

import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;

/**
 * The keys to use with the {@link SettingsModel}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public enum SettingKey {
	/** should the song title be shown in presentation */
	SHOW_TITLE(Boolean.class),
	/** font of song title */
	TITLE_FONT(Font.class),
	/** font of song lyrics (also used for chords if they are shown) */
	LYRICS_FONT(Font.class),
	/** font of song lyrics translation */
	TRANSLATION_FONT(Font.class),
	/** font of song copyright */
	COPYRIGHT_FONT(Font.class),
	/** color of presentation text */
	TEXT_COLOR(Color.class),
	/** color of presentation background */
	BACKGROUND_COLOR(Color.class),
	/** file to use as logo */
	LOGO_FILE(String.class),
	/** top margin for presentation */
	TOP_MARGIN(Integer.class),
	/** left margin for presentation */
	LEFT_MARGIN(Integer.class),
	/** right margin for presentation */
	RIGHT_MARGIN(Integer.class),
	/** bottom margin for presentation */
	BOTTOM_MARGIN(Integer.class),
	/** margin between title and lyrics */
	DISTANCE_TITLE_TEXT(Integer.class),
	/** margin between lyrics and copyright notice */
	DISTANCE_TEXT_COPYRIGHT(Integer.class),
	/** to which content the filter should be applied */
	SONG_LIST_FILTER(FilterTypeEnum.class),
	/** display of first configured presentation screen */
	SCREEN_1_DISPLAY(String.class),
	/** content of first configured presentation screen */
	SCREEN_1_CONTENTS(ScreenContentsEnum.class),
	/** display of second configured presentation screen */
	SCREEN_2_DISPLAY(String.class),
	/** content of second configured presentation screen */
	SCREEN_2_CONTENTS(ScreenContentsEnum.class),
	/** how long the song has to be shown until it counts as presented (for statistics) */
	SECONDS_UNTIL_COUNTED(Integer.class),
	/** directory to use for slide show */
	SLIDE_SHOW_DIRECTORY(String.class),
	/** how long each picture should be displayed in slide show */
	SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE(Integer.class);
	
	private Class<?> type;
	
	private SettingKey(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	}
}
