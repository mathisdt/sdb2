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
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
import org.zephyrsoft.sdb2.model.Song;

/**
 * Renders the contents of a {@link Song} in order to display it on a screen. Scrolling is handled internally - no
 * scrollpane around this component is needed!
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongView extends JComponent implements Scroller {
	
	private static final long serialVersionUID = 4746652382939122421L;
	
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
		foregroundColor = builder.foregroundColor;
		backgroundColor = builder.backgroundColor;
		
		text = new JTextPane();
		((DefaultCaret) text.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		add(text, BorderLayout.CENTER);
		
		render();
	}
	
	/**
	 * Display the song inside the JTextPane using the constraints indicated by the fields, e.g. "showTitle", and create
	 * a list of parts (paragraphs) and lines for the {@link Scroller} methods.
	 */
	private void render() {
		
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
				|| rightMargin == null || bottomMargin == null || foregroundColor == null || backgroundColor == null) {
				throw new IllegalStateException("not every builder method was called with a non-null value");
			}
			
			return new SongView(this);
		}
		
	}
}
