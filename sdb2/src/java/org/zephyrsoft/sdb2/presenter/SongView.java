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
package org.zephyrsoft.sdb2.presenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongParser;

/**
 * Renders the contents of a {@link Song} in order to display it on a screen. Scrolling is handled internally - no
 * scrollpane around this component is needed!
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongView extends JPanel implements Scroller {
	
	private static final long serialVersionUID = 4746652382939122421L;
	
	private static final Style DEFAULT_STYLE = StyleContext.getDefaultStyleContext().getStyle(
		StyleContext.DEFAULT_STYLE);
	private static final String TITLE_LYRICS_DISTANCE = "TITLE_LYRICS_DISTANCE";
	private static final String LYRICS_COPYRIGHT_DISTANCE = "LYRICS_COPYRIGHT_DISTANCE";
	
	private Song song;
	private boolean showTitle;
	private boolean showChords;
	private Font titleFont;
	private Font lyricsFont;
	private Font translationFont;
	private Font copyrightFont;
	private int topMargin;
	private int leftMargin;
	private int rightMargin;
	private int bottomMargin;
	private int titleLyricsDistance;
	private int lyricsCopyrightDistance;
	private Color foregroundColor;
	private Color backgroundColor;
	
	private JTextPane text;
	
	/**
	 * Private constructor: only the builder may call it.
	 */
	private SongView(Builder builder) {
		song = builder.song;
		showTitle = builder.showTitle;
		showChords = builder.showChords;
		titleFont = builder.titleFont;
		lyricsFont = builder.lyricsFont;
		translationFont = builder.translationFont;
		copyrightFont = builder.copyrightFont;
		topMargin = builder.topMargin;
		leftMargin = builder.leftMargin;
		rightMargin = builder.rightMargin;
		bottomMargin = builder.bottomMargin;
		titleLyricsDistance = builder.titleLyricsDistance;
		lyricsCopyrightDistance = builder.lyricsCopyrightDistance;
		foregroundColor = builder.foregroundColor;
		backgroundColor = builder.backgroundColor;
		
		text = new JTextPane();
		((DefaultCaret) text.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		text.setRequestFocusEnabled(false);
		text.setEditable(false);
		text.setEnabled(false);
		
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
	 * Display the song inside the JTextPane using the constraints indicated by the fields, e.g. "showTitle", and create
	 * a list of parts (paragraphs) and lines for the {@link Scroller} methods.
	 */
	private void render() {
		List<SongElement> toDisplay = SongParser.parse(song, showTitle, showChords);
		
		// create styles to use them later
		StyledDocument document = text.getStyledDocument();
		addStyleFromFont(document, SongElementEnum.TITLE.name(), titleFont);
		addStyle(document, TITLE_LYRICS_DISTANCE, false, false, lyricsFont.getFamily(), titleLyricsDistance);
		addStyleFromFont(document, SongElementEnum.LYRICS.name(), lyricsFont);
		addStyleFromFont(document, SongElementEnum.TRANSLATION.name(), translationFont);
		addStyle(document, LYRICS_COPYRIGHT_DISTANCE, false, false, lyricsFont.getFamily(), lyricsCopyrightDistance);
		addStyleFromFont(document, SongElementEnum.COPYRIGHT.name(), copyrightFont);
		
		// handle the elements of the song
		SongElement previousElement = null;
		for (SongElement element : toDisplay) {
			if (previousElement != null && previousElement.getType() != SongElementEnum.COPYRIGHT
				&& element.getType() == SongElementEnum.COPYRIGHT) {
				// first copyright line: prepend space
				appendText(document, "\n", SongElementEnum.LYRICS.name());
				appendText(document, " \n", LYRICS_COPYRIGHT_DISTANCE);
			} else if (previousElement != null && previousElement.getType() == SongElementEnum.COPYRIGHT
				&& element.getType() == SongElementEnum.COPYRIGHT) {
				// another copyright line: prepend newline
				appendText(document, "\n", SongElementEnum.COPYRIGHT.name());
			}
			
			appendText(document, element.getElement(), element.getType().name());
			
			if (element.getType() == SongElementEnum.TITLE) {
				// title line: append space
				appendText(document, "\n", SongElementEnum.TITLE.name());
				appendText(document, " \n", TITLE_LYRICS_DISTANCE);
			}
			
			previousElement = element;
		}
	}
	
	private void appendText(StyledDocument document, String string, String type) {
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
	
	private static Style addStyleFromFont(StyledDocument document, String styleName, Font font) {
		return addStyle(document, styleName, font.isItalic(), font.isBold(), font.getFamily(), font.getSize());
	}
	
	private static Style addStyle(StyledDocument document, String styleName, boolean italic, boolean bold,
		String fontFamily, int fontSize) {
		Style style = document.addStyle(styleName, DEFAULT_STYLE);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setFontFamily(style, fontFamily);
		StyleConstants.setFontSize(style, fontSize);
		return style;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Scroller#moveToPart(java.lang.Integer)
	 */
	@Override
	public void moveToPart(Integer part) {
		// TODO
		
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Scroller#moveToLine(java.lang.Integer)
	 */
	@Override
	public void moveToLine(Integer line) {
		// TODO
		
	}
	
	public static class Builder {
		private Song song;
		private Boolean showTitle;
		private Boolean showChords;
		private Font titleFont;
		private Font lyricsFont;
		private Font translationFont;
		private Font copyrightFont;
		private Integer topMargin;
		private Integer leftMargin;
		private Integer rightMargin;
		private Integer bottomMargin;
		private Integer titleLyricsDistance;
		private Integer lyricsCopyrightDistance;
		private Color foregroundColor;
		private Color backgroundColor;
		
		public Builder(Song song) {
			this.song = song;
		}
		
		public Builder showTitle(Boolean bool) {
			this.showTitle = bool;
			return this;
		}
		
		public Builder showChords(Boolean bool) {
			this.showChords = bool;
			return this;
		}
		
		public Builder titleFont(Font font) {
			this.titleFont = font;
			return this;
		}
		
		public Builder lyricsFont(Font font) {
			this.lyricsFont = font;
			return this;
		}
		
		public Builder translationFont(Font font) {
			this.translationFont = font;
			return this;
		}
		
		public Builder copyrightFont(Font font) {
			this.copyrightFont = font;
			return this;
		}
		
		public Builder topMargin(Integer pixels) {
			this.topMargin = pixels;
			return this;
		}
		
		public Builder leftMargin(Integer pixels) {
			this.leftMargin = pixels;
			return this;
		}
		
		public Builder rightMargin(Integer pixels) {
			this.rightMargin = pixels;
			return this;
		}
		
		public Builder bottomMargin(Integer pixels) {
			this.bottomMargin = pixels;
			return this;
		}
		
		public Builder titleLyricsDistance(Integer pixels) {
			this.titleLyricsDistance = pixels;
			return this;
		}
		
		public Builder lyricsCopyrightDistance(Integer pixels) {
			this.lyricsCopyrightDistance = pixels;
			return this;
		}
		
		public Builder foregroundColor(Color color) {
			this.foregroundColor = color;
			return this;
		}
		
		public Builder backgroundColor(Color color) {
			this.backgroundColor = color;
			return this;
		}
		
		public SongView build() {
			// make sure every variable was initialized
			if (song == null || showTitle == null || showChords == null || titleFont == null || lyricsFont == null
				|| translationFont == null || copyrightFont == null || topMargin == null || leftMargin == null
				|| rightMargin == null || bottomMargin == null || titleLyricsDistance == null
				|| lyricsCopyrightDistance == null || foregroundColor == null || backgroundColor == null) {
				throw new IllegalStateException("not every builder method was called with a non-null value");
			}
			
			return new SongView(this);
		}
		
	}
}
