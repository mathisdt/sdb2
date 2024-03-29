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
package org.zephyrsoft.sdb2.model.settings;

import java.awt.Color;
import java.awt.Font;

import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;

/**
 * The keys to use with the {@link SettingsModel}.
 */
public enum SettingKey {
	/** should the song title be shown in presentation */
	SHOW_TITLE(Boolean.class),
	/** font of song title - first presentation screen */
	TITLE_FONT(Font.class),
	/** font of song lyrics (also used for chords if they are shown) - first presentation screen */
	LYRICS_FONT(Font.class),
	/** font of song lyrics translation - first presentation screen */
	TRANSLATION_FONT(Font.class),
	/** font of song copyright - first presentation screen */
	COPYRIGHT_FONT(Font.class),
	/** font of chord sequence block - first presentation screen */
	CHORD_SEQUENCE_FONT(Font.class),
	/** font of song title - second presentation screen */
	TITLE_FONT_2(Font.class),
	/** font of song lyrics (also used for chords if they are shown) - second presentation screen */
	LYRICS_FONT_2(Font.class),
	/** font of song lyrics translation - second presentation screen */
	TRANSLATION_FONT_2(Font.class),
	/** font of song copyright - second presentation screen */
	COPYRIGHT_FONT_2(Font.class),
	/** font of chord sequence block - second presentation screen */
	CHORD_SEQUENCE_FONT_2(Font.class),
	/** color of presentation text - first presentation screen */
	TEXT_COLOR(Color.class),
	/** color of presentation text - second presentation screen */
	TEXT_COLOR_2(Color.class),
	/** color of presentation background - first presentation screen */
	BACKGROUND_COLOR(Color.class),
	/** color of presentation background - second presentation screen */
	BACKGROUND_COLOR_2(Color.class),
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
	/** should the scrolling be minimized - first presentation screen */
	MINIMAL_SCROLLING(Boolean.class),
	/** should the scrolling be minimized - second presentation screen */
	MINIMAL_SCROLLING_2(Boolean.class),
	/** how long the song has to be shown until it counts as presented (for statistics) */
	SECONDS_UNTIL_COUNTED(Integer.class),
	/** directory to use for slide show */
	SLIDE_SHOW_DIRECTORY(String.class),
	/** how long each picture should be displayed in slide show */
	SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE(Integer.class),
	/** how long is the fade-in/fade-out animation (in milliseconds) */
	FADE_TIME(Integer.class),
	/** where does the calendar reside? */
	CALENDAR_URL(String.class),
	/** how may days should the calendar show? */
	CALENDAR_DAYS_AHEAD(Integer.class),
	/** if remote connection should be used */
	REMOTE_ENABLED(Boolean.class),
	/** server uri for remote connection */
	REMOTE_SERVER(String.class),
	/** user-name for remote connection */
	REMOTE_USERNAME(String.class),
	/** password for remote connection */
	REMOTE_PASSWORD(String.class),
	/** prefix for remote connection */
	REMOTE_PREFIX(String.class),
	/** room for remote connection */
	REMOTE_NAMESPACE(String.class);
	
	private Class<?> type;
	
	private SettingKey(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	}
}
