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

import static org.zephyrsoft.sdb2.model.SongElementEnum.CHORDS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.COPYRIGHT;
import static org.zephyrsoft.sdb2.model.SongElementEnum.LYRICS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.NEW_LINE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TITLE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TRANSLATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
import org.zephyrsoft.sdb2.util.ChordSpaceCorrector;
import org.zephyrsoft.sdb2.util.StringTools;

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
	private static final String NEWLINE_CHAR = "\n";
	private static final String LYRICS_COPYRIGHT_DISTANCE_TEXT = " \n";
	
	private Song song;
	private boolean showTitle;
	private boolean showTranslation;
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
		showTranslation = builder.showTranslation;
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
		List<SongElement> toDisplay = SongParser.parse(song, showTranslation, showTitle, showChords);
		if (toDisplay.size() > 0 && !is(toDisplay.get(toDisplay.size() - 1), COPYRIGHT)) {
			// add final newline to fetch the last line of the last part always
			toDisplay.add(new SongElement(NEW_LINE, NEWLINE_CHAR));
		}
		
		// chord lines: correct spacing (in lyrics font) to make the chords correspond to the words
		FontMetrics fm = createFontMetricsForLyrics();
		ChordSpaceCorrector chordSpaceCorrector = new ChordSpaceCorrector(str -> fm.stringWidth(str));
		SongElement previousChordsElement = null;
		for (SongElement songElement : toDisplay) {
			switch (songElement.getType()) {
				case LYRICS:
					if (previousChordsElement == null) {
						continue;
					}
					previousChordsElement.setElement(
						chordSpaceCorrector.correctChordSpaces(previousChordsElement.getElement(), songElement.getElement()));
					previousChordsElement = null;
					break;
				case CHORDS:
					previousChordsElement = songElement;
					break;
			}
		}
		
		parts = new ArrayList<>();
		AddressablePart currentPart = new AddressablePart();
		Integer position = null;
		
		document = text.getStyledDocument();
		addStyles();
		
		// handle the elements of the song
		SongElement prevPrevElement = null;
		SongElement prevElement = null;
		for (SongElement element : toDisplay) {
			handleCopyrightLine(prevElement, element);
			
			handleTitlePosition(element);
			
			if ((is(element, NEW_LINE) || is(element, COPYRIGHT))
				&& !isEmpty(prevElement)
				&& (!is(prevElement, SongElementEnum.TRANSLATION)
					|| currentPart.isEmpty()) // translation line is first line in part
				&& position == null) {
				position = createPosition(prevElement);
			}
			
			if (isBodyElement(element)) {
				if ((bothAreNewlines(prevElement, element)
					|| (is(element, NEW_LINE)
						&& isEmpty(prevElement)
						&& (prevPrevElement == null || is(prevPrevElement, NEW_LINE))))
					&& currentPart.size() > 0) {
					// [ two consecutive newlines OR two newlines, only separated by a blank line ] AND current part is
					// populated with at least one line => save current part and begin a new one
					parts.add(currentPart);
					currentPart = new AddressablePart();
				} else if (is(element, NEW_LINE)
					&& is(prevElement, LYRICS)
					&& !isEmpty(prevElement)
					&& (prevPrevElement == null || is(prevPrevElement, NEW_LINE) || is(prevPrevElement, TITLE))) {
					// save current line and begin a new one
					AddressableLine currentLine = new AddressableLine(prevElement, position);
					currentPart.add(currentLine);
					position = null;
				}
			} else if (is(element, COPYRIGHT)
				&& isContentElement(prevElement)
				&& !isEmpty(prevElement)
				&& !is(prevElement, SongElementEnum.TRANSLATION)
				&& (prevPrevElement == null || is(prevPrevElement, NEW_LINE))) {
				// save current line and begin a new one
				AddressableLine currentLine = new AddressableLine(prevElement, position);
				currentPart.add(currentLine);
				position = null;
			}
			
			if (bothAreNewlines(prevElement, element)
				|| (is(element, NEW_LINE) && prevElement != null && isEmpty(prevElement)
					&& (prevPrevElement == null || is(prevPrevElement, NEW_LINE)))) {
				appendText(element.getElement(), LYRICS);
			} else {
				appendText(element.getElement(), element.getType());
			}
			
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
	
	private boolean bothAreNewlines(SongElement prevElement, SongElement element) {
		return is(element, NEW_LINE) && is(prevElement, NEW_LINE);
	}
	
	private FontMetrics createFontMetricsForLyrics() {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		return g.getFontMetrics(lyricsFont);
	}
	
	private void handleTitlePosition(SongElement element) {
		if (is(element, TITLE)) {
			Integer position = createPosition();
			AddressablePart titlePart = new AddressablePart();
			titlePart.add(new AddressableLine(element, position));
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
		addStyleFromFont(TITLE.name(), titleFont);
		addStyle(TITLE_LYRICS_DISTANCE, false, false, lyricsFont.getFamily(), titleLyricsDistance);
		addStyleFromFont(CHORDS.name(), lyricsFont);
		addStyleFromFont(LYRICS.name(), lyricsFont);
		addStyleFromFont(TRANSLATION.name(), translationFont);
		addStyle(LYRICS_COPYRIGHT_DISTANCE, false, false, lyricsFont.getFamily(), lyricsCopyrightDistance);
		addStyleFromFont(COPYRIGHT.name(), copyrightFont);
	}
	
	private void handleTitleLine(SongElement element) {
		if (is(element, TITLE)) {
			// append space
			appendText(NEWLINE_CHAR, NEW_LINE);
			appendText(LYRICS_COPYRIGHT_DISTANCE_TEXT, TITLE_LYRICS_DISTANCE);
		}
	}
	
	private static boolean isEmpty(SongElement element) {
		return element == null || StringTools.isBlank(element.getElement());
	}
	
	private static boolean is(SongElement element, SongElementEnum type) {
		return element != null && element.getType() == type;
	}
	
	/** chords, lyrics, translation or newline */
	private static boolean isBodyElement(SongElement element) {
		return is(element, CHORDS) || is(element, LYRICS) || is(element, TRANSLATION) || is(element, NEW_LINE);
	}
	
	/** chords, lyrics or translation */
	private static boolean isContentElement(SongElement element) {
		return is(element, CHORDS) || is(element, LYRICS) || is(element, TRANSLATION);
	}
	
	private void handleCopyrightLine(SongElement previousElement, SongElement element) {
		if (isFirstCopyrightLine(previousElement, element)) {
			// prepend space
			appendText(NEWLINE_CHAR, NEW_LINE);
			appendText(LYRICS_COPYRIGHT_DISTANCE_TEXT, LYRICS_COPYRIGHT_DISTANCE);
		} else if (isCopyrightLineButNotFirstOne(previousElement, element)) {
			// prepend newline
			appendText(NEWLINE_CHAR, NEW_LINE);
		}
	}
	
	private static boolean isCopyrightLineButNotFirstOne(SongElement previousElement, SongElement element) {
		return is(previousElement, COPYRIGHT) && is(element, COPYRIGHT);
	}
	
	private static boolean isFirstCopyrightLine(SongElement previousElement, SongElement element) {
		return !is(previousElement, COPYRIGHT) && is(element, COPYRIGHT);
	}
	
	private void appendText(String string, SongElementEnum type) {
		appendText(string, type.name());
	}
	
	private void appendText(String string, String type) {
		try {
			int offset = document.getLength();
			// add style only if type is anything apart from NEW_LINE
			AttributeSet style = SimpleAttributeSet.EMPTY;
			if (type != null && type != NEW_LINE.name()) {
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
			Rectangle2D target = text.modelToView2D(addressablePart.getPosition());
			animatedMoveTo(new Point((int) text.getLocation().getX(), (int) (topMargin - target.getY())));
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
			Rectangle2D target = text.modelToView2D(addressableLine.getPosition());
			animatedMoveTo(new Point((int) text.getLocation().getX(), (int) (topMargin - target.getY())));
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
		private Boolean showTranslation;
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
		
		public Builder showTranslation(Boolean bool) {
			this.showTranslation = bool;
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
			if (song == null || showTitle == null || showTranslation == null || showChords == null
				|| titleFont == null || lyricsFont == null || translationFont == null || copyrightFont == null
				|| topMargin == null || leftMargin == null || rightMargin == null || bottomMargin == null
				|| titleLyricsDistance == null || lyricsCopyrightDistance == null
				|| foregroundColor == null || backgroundColor == null) {
				throw new IllegalStateException("not every builder method was called with a non-null value");
			}
			
			return new SongView(this);
		}
		
	}
}
