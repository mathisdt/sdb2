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
import java.util.Objects;

import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.VirtualScreen;

/**
 * The settings of a specific virtual screen.
 */
public class VirtualScreenSettingsModel {
	
	private final Color backgroundColor;
	private final int topMargin;
	private final int leftMargin;
	private final int rightMargin;
	private final int bottomMargin;
	private final boolean showTitle;
	private final boolean showTranslation;
	private final boolean showChords;
	private final boolean showChordSequence;
	private final Font titleFont;
	private final Font lyricsFont;
	private final Font chordSequenceFont;
	private final Font translationFont;
	private final Font copyrightFont;
	private final int titleLyricsDistance;
	private final int lyricsCopyrightDistance;
	private final Color foregroundColor;
	private final boolean minimalScrolling;
	
	private VirtualScreenSettingsModel(SettingsModel settings, VirtualScreen virtualScreen) {
		backgroundColor = virtualScreen.getBackgroundColor(settings);
		topMargin = settings.get(SettingKey.TOP_MARGIN, Integer.class);
		leftMargin = settings.get(SettingKey.LEFT_MARGIN, Integer.class);
		rightMargin = settings.get(SettingKey.RIGHT_MARGIN, Integer.class);
		bottomMargin = settings.get(SettingKey.BOTTOM_MARGIN, Integer.class);
		showTitle = settings.get(SettingKey.SHOW_TITLE, Boolean.class).booleanValue();
		ScreenContentsEnum contents = virtualScreen.getScreenContents(settings);
		showTranslation = contents.shouldShowTranslation();
		showChords = contents.shouldShowChords();
		showChordSequence = contents.shouldShowChordSequence();
		titleFont = virtualScreen.getTitleFont(settings);
		lyricsFont = virtualScreen.getLyricsFont(settings);
		chordSequenceFont = virtualScreen.getChordSequenceFont(settings);
		translationFont = virtualScreen.getTranslationFont(settings);
		copyrightFont = virtualScreen.getCopyrightFont(settings);
		titleLyricsDistance = settings.get(SettingKey.DISTANCE_TITLE_TEXT, Integer.class);
		lyricsCopyrightDistance = settings.get(SettingKey.DISTANCE_TEXT_COPYRIGHT, Integer.class);
		foregroundColor = virtualScreen.getTextColor(settings);
		minimalScrolling = virtualScreen.getMinimalScrolling(settings);
	}
	
	public static VirtualScreenSettingsModel of(SettingsModel settings, VirtualScreen screen) {
		return new VirtualScreenSettingsModel(settings, screen);
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public int getTopMargin() {
		return topMargin;
	}
	
	public int getLeftMargin() {
		return leftMargin;
	}
	
	public int getRightMargin() {
		return rightMargin;
	}
	
	public int getBottomMargin() {
		return bottomMargin;
	}
	
	public boolean isShowTitle() {
		return showTitle;
	}
	
	public boolean isShowTranslation() {
		return showTranslation;
	}
	
	public boolean isShowChords() {
		return showChords;
	}
	
	public boolean isShowChordSequence() {
		return showChordSequence;
	}
	
	public Font getTitleFont() {
		return titleFont;
	}
	
	public Font getLyricsFont() {
		return lyricsFont;
	}
	
	public Font getChordSequenceFont() {
		return chordSequenceFont;
	}
	
	public Font getTranslationFont() {
		return translationFont;
	}
	
	public Font getCopyrightFont() {
		return copyrightFont;
	}
	
	public int getTitleLyricsDistance() {
		return titleLyricsDistance;
	}
	
	public int getLyricsCopyrightDistance() {
		return lyricsCopyrightDistance;
	}
	
	public Color getForegroundColor() {
		return foregroundColor;
	}
	
	public boolean isMinimalScrolling() {
		return minimalScrolling;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(backgroundColor, bottomMargin, chordSequenceFont, copyrightFont, foregroundColor, leftMargin,
			lyricsCopyrightDistance, lyricsFont, minimalScrolling, rightMargin, showChords, showChordSequence, showTitle,
			showTranslation, titleFont, titleLyricsDistance, topMargin, translationFont);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VirtualScreenSettingsModel other = (VirtualScreenSettingsModel) obj;
		return Objects.equals(backgroundColor, other.backgroundColor)
			&& bottomMargin == other.bottomMargin
			&& Objects.equals(chordSequenceFont, other.chordSequenceFont)
			&& Objects.equals(copyrightFont, other.copyrightFont)
			&& Objects.equals(foregroundColor, other.foregroundColor)
			&& leftMargin == other.leftMargin
			&& lyricsCopyrightDistance == other.lyricsCopyrightDistance
			&& Objects.equals(lyricsFont, other.lyricsFont)
			&& minimalScrolling == other.minimalScrolling
			&& rightMargin == other.rightMargin
			&& showChords == other.showChords
			&& showChordSequence == other.showChordSequence
			&& showTitle == other.showTitle
			&& showTranslation == other.showTranslation
			&& Objects.equals(titleFont, other.titleFont)
			&& titleLyricsDistance == other.titleLyricsDistance
			&& topMargin == other.topMargin
			&& Objects.equals(translationFont, other.translationFont);
	}
	
}
