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
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.AddressableLine;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongElementHistory;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.sdb2.util.ChordSpaceCorrector;
import org.zephyrsoft.sdb2.util.StringTools;

import com.google.common.base.Preconditions;

/**
 * Renders the contents of a {@link Song} in order to display it on a screen. Scrolling is handled internally - no
 * scrollpane around this component is needed!
 */
public class SongView extends JPanel implements Scroller {
	private static final Logger LOG = LoggerFactory.getLogger(SongView.class);
	
	private static final String SONG_PARTS_NOT_INITIALIZED = "the song parts are not initialized";
	
	private static final long serialVersionUID = 4746652382939122421L;
	
	private static final Style DEFAULT_STYLE = StyleContext.getDefaultStyleContext().getStyle(
		StyleContext.DEFAULT_STYLE);
	private static final String TITLE_LYRICS_DISTANCE = "TITLE_LYRICS_DISTANCE";
	private static final String LYRICS_COPYRIGHT_DISTANCE = "LYRICS_COPYRIGHT_DISTANCE";
	private static final String INDENTATION_STYLE = "INDENTATION_STYLE";
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
	private boolean minimalScrolling;
	
	private final JTextPane text;
	private List<AddressablePart> parts;
	
	private StyledDocument document;
	
	private Animator animator;
	private Point animatorTarget;
	
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
		minimalScrolling = builder.minimalScrolling;
		
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
		
		text.setSize(getWidth(), getHeight());
		text.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, 0, rightMargin));
		
		parts = render(song, showTranslation, showTitle, showChords, text.getStyledDocument(), titleFont, lyricsFont, translationFont, copyrightFont,
			titleLyricsDistance, lyricsCopyrightDistance);
		
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
	 * Render the song inside the styled document (e.g. from a JTextPane) using the given constraints,
	 * e.g. "showTitle", and create a list of addressable parts (paragraphs) and lines for the {@link Scroller} methods.
	 */
	public static List<AddressablePart> render(Song song, boolean showTranslation, boolean showTitle, boolean showChords,
		StyledDocument document, Font titleFont, Font lyricsFont, Font translationFont, Font copyrightFont,
		int titleLyricsDistance, int lyricsCopyrightDistance) {
		List<SongElement> toDisplay = SongParser.parse(song, showTranslation, showTitle, showChords);
		SongElementHistory elements = new SongElementHistory(toDisplay);
		
		// chord lines: correct spacing (in lyrics font) to make the chords correspond to the words
		FontMetrics lyricsFontMetrics = createFontMetrics(lyricsFont);
		ChordSpaceCorrector chordSpaceCorrector = new ChordSpaceCorrector(lyricsFontMetrics::stringWidth);
		SongElement previousChordsElement = null;
		for (SongElement songElement : toDisplay) {
			switch (songElement.getType()) {
				case LYRICS -> {
					if (previousChordsElement == null) {
						continue;
					}
					previousChordsElement.setContent(
						chordSpaceCorrector.correctChordSpaces(previousChordsElement.getContent(), songElement.getContent()));
					previousChordsElement = null;
				}
				case CHORDS -> {
					previousChordsElement = songElement;
				}
			}
		}
		
		List<AddressablePart> parts = new ArrayList<>();
		AddressablePart currentPart = new AddressablePart();
		Integer position = null;
		
		addStyles(document, titleFont, lyricsFont, translationFont, copyrightFont, titleLyricsDistance, lyricsCopyrightDistance);
		
		// handle the elements of the song
		for (SongElement element : elements) {
			// TODO use elements.query()...
			SongElement back1 = elements.back(1);
			SongElement back2 = elements.back(2);
			
			handleCopyrightLine(document, back1, element);
			
			handleTitlePosition(document, parts, element);
			
			if ((is(element, NEW_LINE, COPYRIGHT))
				&& (!isEmpty(back1))
				&& (!is(back1, TRANSLATION)
					|| currentPart.isEmpty()) // translation line is first line in part
				&& position == null) {
				position = createPosition(document, back1);
			} else if ((is(element, NEW_LINE, COPYRIGHT))
				&& (!isEmpty(back1))
				&& is(back1, TRANSLATION)
				&& is(back2, LYRICS)
				&& position == null) {
				position = createPosition(document, back2);
			} else if (is(back2, NEW_LINE)
				&& is(back1, NEW_LINE)
				&& !is(element, NEW_LINE)
				&& position == null) {
				position = createPosition(document);
			}
			
			if (isBodyElement(element)) {
				if ((bothAreNewlines(back1, element)
					|| (is(element, NEW_LINE)
						&& isEmpty(back1)
						&& (back2 == null || is(back2, NEW_LINE))))
					&& currentPart.size() > 0) {
					// [ two consecutive newlines OR two newlines, only separated by a blank line ] AND current part is
					// populated with at least one line => save current part and begin a new one
					parts.add(currentPart);
					currentPart = new AddressablePart();
				} else if (is(element, NEW_LINE)
					&& is(back1, LYRICS)) {
					// save current line and begin a new one
					AddressableLine currentLine = new AddressableLine(back1, position);
					currentPart.add(currentLine);
					position = null;
				} else if (is(element, NEW_LINE)
					&& is(back2, LYRICS)
					&& is(back1, TRANSLATION)) {
					// save current line and begin a new one
					AddressableLine currentLine = new AddressableLine(back2, position);
					currentPart.add(currentLine);
					position = null;
				}
			} else if (is(element, COPYRIGHT)
				&& isContentElement(back1)
				&& !isEmpty(back1)
				&& !is(back1, TRANSLATION)
				&& (back2 == null || is(back2, NEW_LINE))) {
				// save current line and begin a new one
				AddressableLine currentLine = new AddressableLine(back1, position);
				currentPart.add(currentLine);
				position = null;
			}
			
			if (bothAreNewlines(back1, element)
				|| (is(element, NEW_LINE) && back1 != null && isEmpty(back1)
					&& (back2 == null || is(back2, NEW_LINE)))) {
				appendText(document, element.getContent(), LYRICS, element.getIndentation());
			} else {
				appendText(document, element.getContent(), element.getType(), element.getIndentation());
			}
			
			handleTitleLine(document, element);
		}
		if (currentPart.size() > 0) {
			// current part is populated with at least one line => save current part
			parts.add(currentPart);
		}
		return parts;
	}
	
	private static boolean bothAreNewlines(SongElement prevElement, SongElement element) {
		return is(element, NEW_LINE) && is(prevElement, NEW_LINE);
	}
	
	private static FontMetrics createFontMetrics(Font font) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		return g.getFontMetrics(font);
	}
	
	private static void handleTitlePosition(StyledDocument document, List<AddressablePart> parts, SongElement element) {
		if (is(element, TITLE)) {
			Integer position = createPosition(document);
			AddressablePart titlePart = new AddressablePart();
			titlePart.add(new AddressableLine(element, position));
			parts.add(titlePart);
		}
	}
	
	private static Integer createPosition(StyledDocument document, SongElement... toSubtract) {
		int toSubtractInt = 0;
		for (SongElement element : toSubtract) {
			toSubtractInt += element.getContent() == null ? 0 : element.getContent().length();
		}
		// +1 because the position "before the first character on the line" is still on the line before
		return document.getLength() - toSubtractInt + 1;
	}
	
	private static void addStyles(StyledDocument document, Font titleFont, Font lyricsFont, Font translationFont,
		Font copyrightFont, int titleLyricsDistance, int lyricsCopyrightDistance) {
		addStyleFromFont(document, TITLE.name(), titleFont);
		addStyle(document, TITLE_LYRICS_DISTANCE, false, false, lyricsFont.getFamily(), titleLyricsDistance);
		addStyleFromFont(document, CHORDS.name(), lyricsFont);
		addStyleFromFont(document, LYRICS.name(), lyricsFont);
		addStyleFromFont(document, TRANSLATION.name(), translationFont);
		addStyle(document, LYRICS_COPYRIGHT_DISTANCE, false, false, lyricsFont.getFamily(), lyricsCopyrightDistance);
		addStyleFromFont(document, COPYRIGHT.name(), copyrightFont);
		int indentationFontSize = Math.min(lyricsFont.getSize(), translationFont.getSize());
		addStyle(document, INDENTATION_STYLE, false, false, lyricsFont.getFamily(), indentationFontSize);
	}
	
	private static void handleTitleLine(StyledDocument document, SongElement element) {
		if (is(element, TITLE)) {
			// append space
			appendText(document, NEWLINE_CHAR, NEW_LINE, 0);
			appendText(document, LYRICS_COPYRIGHT_DISTANCE_TEXT, TITLE_LYRICS_DISTANCE, 0);
		}
	}
	
	private static boolean isEmpty(SongElement element) {
		return element == null || StringTools.isBlank(element.getContent());
	}
	
	public static boolean is(SongElement element, SongElementEnum... songElementEnums) {
		return element != null && Stream.of(songElementEnums)
			.anyMatch(e -> element.getType() == e);
	}
	
	/** chords, lyrics, translation or newline */
	private static boolean isBodyElement(SongElement element) {
		return is(element, CHORDS, LYRICS, TRANSLATION, NEW_LINE);
	}
	
	/** chords, lyrics or translation */
	private static boolean isContentElement(SongElement element) {
		return is(element, CHORDS, LYRICS, TRANSLATION);
	}
	
	private static void handleCopyrightLine(StyledDocument document, SongElement previousElement, SongElement element) {
		if (isFirstCopyrightLine(previousElement, element)) {
			// prepend space
			appendText(document, NEWLINE_CHAR, NEW_LINE, 0);
			appendText(document, LYRICS_COPYRIGHT_DISTANCE_TEXT, LYRICS_COPYRIGHT_DISTANCE, 0);
		} else if (isCopyrightLineButNotFirstOne(previousElement, element)) {
			// prepend newline
			appendText(document, NEWLINE_CHAR, NEW_LINE, 0);
		}
	}
	
	private static boolean isCopyrightLineButNotFirstOne(SongElement previousElement, SongElement element) {
		return is(previousElement, COPYRIGHT) && is(element, COPYRIGHT);
	}
	
	private static boolean isFirstCopyrightLine(SongElement previousElement, SongElement element) {
		return !is(previousElement, COPYRIGHT) && is(element, COPYRIGHT);
	}
	
	private static void appendText(StyledDocument document, String string, SongElementEnum type, int indentation) {
		appendText(document, string, type.name(), indentation);
	}
	
	private static void appendText(StyledDocument document, String string, String type, int indentation) {
		try {
			if (indentation > 0) {
				int offset = document.getLength();
				AttributeSet style = document.getStyle(INDENTATION_STYLE);
				document.insertString(offset, "   ".repeat(indentation), style);
			}
			
			int offset = document.getLength();
			// add style only if type is anything apart from NEW_LINE
			AttributeSet style = SimpleAttributeSet.EMPTY;
			if (type != null && !type.equals(NEW_LINE.name())) {
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
	
	private static Style addStyle(StyledDocument document, String styleName, boolean italic, boolean bold, String fontFamily, int fontSize) {
		Style style = document.addStyle(styleName, DEFAULT_STYLE);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setFontFamily(style, fontFamily);
		StyleConstants.setFontSize(style, fontSize);
		return style;
	}
	
	@Override
	public boolean hasParts() {
		return parts != null && !parts.isEmpty();
	}
	
	@Override
	public List<AddressablePart> getParts() {
		Preconditions.checkArgument(parts != null, SONG_PARTS_NOT_INITIALIZED);
		return parts;
	}
	
	@Override
	public void moveToPart(Integer part) {
		moveToPart(part, true);
	}
	
	public void moveToPart(Integer part, boolean animated) {
		Preconditions.checkArgument(parts != null, SONG_PARTS_NOT_INITIALIZED);
		adjustHeightIfNecessary();
		try {
			int noTitlePart = showTitle ? part : Math.max(part - 1, 0);
			AddressablePart addressablePart = parts.get(noTitlePart);
			Preconditions.checkArgument(addressablePart != null, "part index does not correspond to a part of the song");
			Rectangle2D target = text.modelToView2D(addressablePart.getPosition());
			double targetY = target.getY();
			if (minimalScrolling && targetY > text.getPreferredSize().getHeight() + topMargin + bottomMargin - getSize().getHeight()) {
				targetY = text.getPreferredSize().getHeight() + topMargin + bottomMargin - getSize().getHeight();
			}
			Point targetLocation = new Point((int) text.getLocation().getX(), (int) Math.min(0, (topMargin - targetY)));
			LOG.debug("moving to part {} (animated={}) - target={}", part, animated, targetLocation);
			if (animated) {
				animatedMoveTo(targetLocation);
			} else {
				abruptMoveTo(targetLocation);
			}
		} catch (BadLocationException e) {
			throw new IllegalStateException("could not identify position in text", e);
		}
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		moveToLine(part, line, true);
	}
	
	public void moveToLine(Integer partNullable, Integer lineNullable, boolean animated) {
		Preconditions.checkArgument(parts != null, SONG_PARTS_NOT_INITIALIZED);
		
		int part = partNullable == null ? 0 : partNullable;
		int line = lineNullable == null ? 0 : lineNullable;
		
		adjustHeightIfNecessary();
		try {
			int noTitlePart = showTitle ? part : Math.max(part - 1, 0);
			AddressablePart addressablePart = parts.get(noTitlePart);
			Preconditions.checkArgument(addressablePart != null, "part index does not correspond to a part of the song");
			AddressableLine addressableLine = addressablePart.get(line);
			Preconditions.checkArgument(addressableLine != null, "line index does not correspond to a line of the addressed part");
			Rectangle2D target = text.modelToView2D(addressableLine.getPosition());
			double targetY = target.getY();
			if (minimalScrolling && targetY > text.getPreferredSize().getHeight() + topMargin + bottomMargin - getSize().getHeight()) {
				targetY = text.getPreferredSize().getHeight() + topMargin + bottomMargin - getSize().getHeight();
			}
			Point targetLocation = new Point((int) text.getLocation().getX(), (int) Math.min(0, (topMargin - targetY)));
			LOG.debug("moving to part {} / line {} (animated={}) - target={}", part, line, animated, targetLocation);
			if (animated) {
				animatedMoveTo(targetLocation);
			} else {
				abruptMoveTo(targetLocation);
			}
		} catch (BadLocationException e) {
			throw new IllegalStateException("could not identify position in text", e);
		}
	}
	
	@Override
	public void moveTo(SongPresentationPosition position) {
		moveToLine(position.getPartIndex(), position.getLineIndex());
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
		animator = createAnimator(1200);
		TimingTargetAdapter target = PropertySetter.getTargetTo(text, "location",
			new AccelerationInterpolator(0.5, 0.5), targetLocation);
		animator.addTarget(target);
		animator.start();
	}
	
	private void abruptMoveTo(Point targetLocation) {
		SwingUtilities.invokeLater(() -> {
			adjustHeightIfNecessary();
			text.setLocation(targetLocation);
		});
	}
	
	public int getScrollPosition() {
		return (int) text.getLocation().getY();
	}
	
	private Animator createAnimator(long duration) {
		return new Animator.Builder().setDuration(duration, TimeUnit.MILLISECONDS).build();
	}
	
	private void adjustHeightIfNecessary() {
		try {
			text.validate();
			// adjust height to meet at least the required value so that all text is visible
			if (text.getSize().height < text.getPreferredSize().height) {
				text.setSize(text.getSize().width, text.getPreferredSize().height);
			}
		} catch (NullPointerException e) {
			// This is a fix for remote, where moveToLine is called too fast after startup
		} catch (ArrayIndexOutOfBoundsException e) {
			// fix a Swing problem with layouting (sometimes getPreferredSize() throws this exception)
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
		private Boolean minimalScrolling;
		
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
		
		public Builder minimalScrolling(Boolean bool) {
			this.minimalScrolling = bool;
			return this;
		}
		
		public SongView build() {
			// make sure every variable was initialized
			if (song == null || showTitle == null || showTranslation == null || showChords == null
				|| titleFont == null || lyricsFont == null || translationFont == null || copyrightFont == null
				|| topMargin == null || leftMargin == null || rightMargin == null || bottomMargin == null
				|| titleLyricsDistance == null || lyricsCopyrightDistance == null
				|| foregroundColor == null || backgroundColor == null || minimalScrolling == null) {
				throw new IllegalStateException("not every builder method was called with a non-null value");
			}
			
			return new SongView(this);
		}
		
	}
}
