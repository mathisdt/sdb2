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
package org.zephyrsoft.sdb2.presenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.MatteBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.springframework.util.StringUtils;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;

/**
 * Renders the chord sequence of a {@link Song} in order to display it on a screen.
 */
public class ChordSequenceView extends JPanel {
	
	private static final long serialVersionUID = 4746652382939122422L;
	
	private static final Style DEFAULT_STYLE = StyleContext.getDefaultStyleContext().getStyle(
		StyleContext.DEFAULT_STYLE);
	
	private Song song;
	private Font chordSequenceFont;
	private Color foregroundColor;
	private Color backgroundColor;
	
	private JTextPane text;
	
	private StyledDocument document;
	
	/**
	 * Private constructor: only the builder may call it.
	 */
	public ChordSequenceView(Song song, Font chordSequenceFont, Color foregroundColor, Color backgroundColor) {
		this.song = song;
		this.chordSequenceFont = chordSequenceFont;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
		
		text = new JTextPane();
		((DefaultCaret) text.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		text.setRequestFocusEnabled(false);
		text.setEditable(false);
		text.setEnabled(false);
		
		if (StringUtils.hasText(song.getChordSequence())) {
			setBorder(new MatteBorder(1, 0, 0, 0, foregroundColor));
		}
		
		setLayout(new BorderLayout());
		add(text, BorderLayout.CENTER);
		setBackground(backgroundColor);
		setOpaque(true);
		text.setForeground(foregroundColor);
		text.setDisabledTextColor(foregroundColor);
		
		render();
		
		// workaround for Nimbus L&F:
		text.setOpaque(false);
		text.setBackground(new Color(0, 0, 0, 0));
	}
	
	/**
	 * Display the chord sequence inside the JTextPane.
	 */
	private void render() {
		document = text.getStyledDocument();
		addStyles();
		appendText(song.getChordSequence(), SongElementEnum.CHORDS.name());
	}
	
	private Integer createPosition(SongElement... toSubtract) {
		int toSubtractInt = 0;
		for (SongElement element : toSubtract) {
			toSubtractInt += element.getContent() == null ? 0 : element.getContent().length();
		}
		return document.getLength() - toSubtractInt + 1;
	}
	
	private void addStyles() {
		addStyleFromFont(SongElementEnum.CHORDS.name(), chordSequenceFont);
	}
	
	private void appendText(String string, String type) {
		try {
			int offset = document.getLength();
			// add style only if type is anything apart from NEW_LINE
			AttributeSet style = SimpleAttributeSet.EMPTY;
			if (type != null && type != SongElementEnum.NEW_LINE.name()) {
				style = document.getStyle(type);
			}
			document.insertString(offset, string, style);
		} catch (BadLocationException e) {
			throw new IllegalStateException("could not insert text into document", e);
		}
	}
	
	private Style addStyleFromFont(String styleName, Font font) {
		return addStyle(styleName, font.isItalic(), font.isBold(), font.getFamily(), font.getSize());
	}
	
	private Style addStyle(String styleName, boolean italic, boolean bold, String fontFamily, int fontSize) {
		Style style = document.addStyle(styleName, DEFAULT_STYLE);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setFontFamily(style, fontFamily);
		StyleConstants.setFontSize(style, fontSize);
		return style;
	}
	
	private void adjustHeightIfNecessary() {
		// adjust height to meet at least the required value so that all text is visible
		if (text.getSize().height < text.getPreferredSize().height) {
			text.setSize(text.getSize().width, text.getPreferredSize().height);
		}
	}
	
}
