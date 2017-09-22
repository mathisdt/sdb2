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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
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

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.PropertySetter;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.core.animation.timing.interpolators.AccelerationInterpolator;
import org.zephyrsoft.sdb2.model.AddressableLine;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.util.StringTools;

import com.google.common.base.Preconditions;

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
	private static final String LYRICS_FINAL_NEWLINE = "\n";
	private static final String LYRICS_COPYRIGHT_DISTANCE_TEXT = " \n";
	
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
	private List<AddressablePart> parts;
	
	private StyledDocument document;
	
	protected Animator animator;
	protected Point animatorTarget;
	
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
		
		text.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, 0, rightMargin));
		
		render();
		
		// workaround for Nimbus L&F:
		text.setOpaque(false);
		text.setBackground(new Color(0, 0, 0, 0));
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		int topBorderHeight = topMargin;
		int bottomBorderHeight = bottomMargin;
		
		// gradient upper border as overlay
		Area areaUpper = new Area(new Rectangle2D.Double(0, 0, getWidth(), topBorderHeight));
		g2d.setPaint(new GradientPaint(0, 0, new Color(backgroundColor.getRed(), backgroundColor.getGreen(),
			backgroundColor.getBlue(), 255), 0, topBorderHeight, new Color(backgroundColor.getRed(), backgroundColor
				.getGreen(), backgroundColor.getBlue(), 0), false));
		g2d.fill(areaUpper);
		
		// gradient lower border as overlay
		Area areaLower = new Area(new Rectangle2D.Double(0, getHeight() - bottomBorderHeight, getWidth(), getHeight()));
		g2d.setPaint(new GradientPaint(0, getHeight() - bottomBorderHeight, new Color(backgroundColor.getRed(),
			backgroundColor.getGreen(), backgroundColor.getBlue(), 0), 0, getHeight(), new Color(backgroundColor
				.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 255), false));
		g2d.fill(areaLower);
	}
	
	/**
	 * Display the song inside the JTextPane using the constraints indicated by the fields, e.g. "showTitle", and create
	 * a list of addressable parts (paragraphs) and lines for the {@link Scroller} methods.
	 */
	private void render() {
		List<SongElement> toDisplay = SongParser.parse(song, showTitle, showChords);
		if (toDisplay.size() > 0 && toDisplay.get(toDisplay.size() - 1).getType() != SongElementEnum.COPYRIGHT) {
			// add final newline to fetch the last line of the last part always
			toDisplay.add(new SongElement(SongElementEnum.NEW_LINE, LYRICS_FINAL_NEWLINE));
		}
		
		// TODO chord lines: correct font and correct spacing to correspond to the words below the chords!
		
		parts = new ArrayList<>();
		AddressablePart currentPart = new AddressablePart();
		String currentLineText = null;
		
		document = text.getStyledDocument();
		addStyles();
		
		// handle the elements of the song
		SongElement prevPrevElement = null;
		SongElement prevElement = null;
		for (SongElement element : toDisplay) {
			handleCopyrightLine(prevElement, element);
			
			handleTitlePosition(element);
			if (isBodyElement(element)) {
				if (((element.getType() == SongElementEnum.NEW_LINE && prevElement != null && prevElement.getType() == SongElementEnum.NEW_LINE)
					|| (element.getType() == SongElementEnum.NEW_LINE
						&& prevElement != null
						&& StringTools.isBlank(prevElement.getElement()) && ((prevPrevElement != null && prevPrevElement
							.getType() == SongElementEnum.NEW_LINE) || prevPrevElement == null)))
					&& currentPart.size() > 0) {
					// [ two consecutive newlines OR two newlines, only separated by a blank line ] AND current part is
					// populated with at least one line => save current part and begin a new one
					parts.add(currentPart);
					currentPart = new AddressablePart();
				} else if (element.getType() == SongElementEnum.NEW_LINE
					&& prevElement != null
					&& prevElement.getType() == SongElementEnum.LYRICS
					&& !StringTools.isBlank(prevElement.getElement())
					&& ((prevPrevElement != null && (prevPrevElement.getType() == SongElementEnum.NEW_LINE || prevPrevElement
						.getType() == SongElementEnum.TITLE)) || prevPrevElement == null)) {
					// two newlines OR a title element and a newline, separated by a non-blank lyrics line =>
					// save current line and begin a new one
					currentLineText = prevElement.getElement();
					AddressableLine currentLine = new AddressableLine(currentLineText, createPosition(element, prevElement));
					currentPart.add(currentLine);
				}
			} else if (element.getType() == SongElementEnum.COPYRIGHT
				&& prevElement != null
				&& prevElement.getType() == SongElementEnum.LYRICS
				&& !StringTools.isBlank(prevElement.getElement())
				&& ((prevPrevElement != null && prevPrevElement.getType() == SongElementEnum.NEW_LINE) || prevPrevElement == null)) {
				// a newline and a copyright element, separated by a non-blank lyrics line =>
				// save current line and begin a new one
				currentLineText = prevElement.getElement();
				AddressableLine currentLine = new AddressableLine(currentLineText, createPosition(prevElement)
					- LYRICS_COPYRIGHT_DISTANCE_TEXT.length() - LYRICS_FINAL_NEWLINE.length());
				currentPart.add(currentLine);
			}
			
			String type = element.getType().name();
			if ((element.getType() == SongElementEnum.NEW_LINE && prevElement != null && prevElement.getType() == SongElementEnum.NEW_LINE)
				|| (element.getType() == SongElementEnum.NEW_LINE && prevElement != null
					&& StringTools.isBlank(prevElement.getElement()) && ((prevPrevElement != null && prevPrevElement
						.getType() == SongElementEnum.NEW_LINE) || prevPrevElement == null))) {
				type = SongElementEnum.LYRICS.name();
			}
			appendText(element.getElement(), type);
			
			handleTitleLine(element);
			// keep history
			prevPrevElement = prevElement;
			prevElement = element;
		}
		if (currentPart.size() > 0) {
			// current part is populated with at least one line => save current part
			parts.add(currentPart);
		}
		
	}
	
	private void handleTitlePosition(SongElement element) {
		if (element.getType() == SongElementEnum.TITLE) {
			Integer position = createPosition();
			AddressablePart titlePart = new AddressablePart();
			titlePart.add(new AddressableLine(element.getElement(), position));
			parts.add(titlePart);
		}
	}
	
	private Integer createPosition(SongElement... toSubtract) {
		int toSubtractInt = 0;
		for (SongElement element : toSubtract) {
			toSubtractInt += element.getElement() == null ? 0 : element.getElement().length();
		}
		return document.getLength() - toSubtractInt + 1;
	}
	
	private void addStyles() {
		addStyleFromFont(SongElementEnum.TITLE.name(), titleFont);
		addStyle(TITLE_LYRICS_DISTANCE, false, false, lyricsFont.getFamily(), titleLyricsDistance);
		addStyleFromFont(SongElementEnum.LYRICS.name(), lyricsFont);
		addStyleFromFont(SongElementEnum.TRANSLATION.name(), translationFont);
		addStyle(LYRICS_COPYRIGHT_DISTANCE, false, false, lyricsFont.getFamily(), lyricsCopyrightDistance);
		addStyleFromFont(SongElementEnum.COPYRIGHT.name(), copyrightFont);
	}
	
	private void handleTitleLine(SongElement element) {
		if (isTitleLine(element)) {
			// append space
			appendText(LYRICS_FINAL_NEWLINE, SongElementEnum.NEW_LINE.name());
			appendText(LYRICS_COPYRIGHT_DISTANCE_TEXT, TITLE_LYRICS_DISTANCE);
		}
	}
	
	private static boolean isTitleLine(SongElement element) {
		return element.getType() == SongElementEnum.TITLE;
	}
	
	private static boolean isBodyElement(SongElement element) {
		return element.getType() == SongElementEnum.CHORDS || element.getType() == SongElementEnum.LYRICS
			|| element.getType() == SongElementEnum.TRANSLATION || element.getType() == SongElementEnum.NEW_LINE;
	}
	
	private void handleCopyrightLine(SongElement previousElement, SongElement element) {
		if (isFirstCopyrightLine(previousElement, element)) {
			// prepend space
			appendText(LYRICS_FINAL_NEWLINE, SongElementEnum.NEW_LINE.name());
			appendText(LYRICS_COPYRIGHT_DISTANCE_TEXT, LYRICS_COPYRIGHT_DISTANCE);
		} else if (isCopyrightLineButNotFirstOne(previousElement, element)) {
			// prepend newline
			appendText(LYRICS_FINAL_NEWLINE, SongElementEnum.NEW_LINE.name());
		}
	}
	
	private static boolean isCopyrightLineButNotFirstOne(SongElement previousElement, SongElement element) {
		return previousElement != null && previousElement.getType() == SongElementEnum.COPYRIGHT
			&& element.getType() == SongElementEnum.COPYRIGHT;
	}
	
	private static boolean isFirstCopyrightLine(SongElement previousElement, SongElement element) {
		return previousElement != null && previousElement.getType() != SongElementEnum.COPYRIGHT
			&& element.getType() == SongElementEnum.COPYRIGHT;
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
	
	@Override
	public List<AddressablePart> getParts() {
		Preconditions.checkArgument(parts != null, "the song parts are not initialized");
		return parts;
	}
	
	@Override
	public void moveToPart(Integer part) {
		Preconditions.checkArgument(parts != null, "the song parts are not initialized");
		adjustHeightIfNecessary();
		try {
			AddressablePart addressablePart = parts.get(part);
			Preconditions.checkArgument(addressablePart != null, "part index does not correspond to a part of the song");
			Rectangle target = text.modelToView(addressablePart.getPosition());
			animatedMoveTo(new Point(text.getLocation().x, topMargin - target.y));
		} catch (BadLocationException e) {
			throw new IllegalStateException("could not identify position in text", e);
		}
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		Preconditions.checkArgument(parts != null, "the song parts are not initialized");
		adjustHeightIfNecessary();
		try {
			AddressablePart addressablePart = parts.get(part);
			Preconditions.checkArgument(addressablePart != null, "part index does not correspond to a part of the song");
			AddressableLine addressableLine = addressablePart.get(line);
			Preconditions.checkArgument(addressableLine != null, "line index does not correspond to a line of the addressed part");
			Rectangle target = text.modelToView(addressableLine.getPosition());
			animatedMoveTo(new Point(text.getLocation().x, topMargin - target.y));
		} catch (BadLocationException e) {
			throw new IllegalStateException("could not identify position in text", e);
		}
	}
	
	private void animatedMoveTo(Point targetLocation) {
		if (animator != null && animator.isRunning() && animatorTarget != null && animatorTarget.equals(targetLocation)) {
			// animator is already moving the song text to the requested location
			return;
		} else {
			animatorTarget = targetLocation;
		}
		if (animator != null && animator.isRunning()) {
			animator.stop();
			// discard old animator because it takes too long to let it stop completely
			animator = null;
		}
		animator = createAnimator();
		TimingTargetAdapter target = PropertySetter.getTargetTo(text, "location",
			new AccelerationInterpolator(0.5, 0.5), targetLocation);
		animator.addTarget(target);
		animator.start();
	}
	
	private Animator createAnimator() {
		return new Animator.Builder().setDuration(1200, TimeUnit.MILLISECONDS).build();
	}
	
	private void adjustHeightIfNecessary() {
		// adjust height to meet at least the required value so that all text is visible
		if (text.getSize().height < text.getPreferredSize().height) {
			text.setSize(text.getSize().width, text.getPreferredSize().height);
		}
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
