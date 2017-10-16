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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SelectableScreen;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.util.gui.ImagePanel;

/**
 * The presentation display for the lyrics.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PresenterWindow extends JFrame implements Presenter {
	private static final long serialVersionUID = -2390663756699128439L;
	
	private static final Logger LOG = LoggerFactory.getLogger(PresenterWindow.class);
	
	private JPanel contentPane;
	private SongView songView;
	
	private final SettingsModel settings;
	private final SelectableScreen screen;
	private final ScreenContentsEnum contents;
	private final Presentable presentable;
	
	private Cursor transparentCursor;
	
	private Color backgroundColor;
	
	public PresenterWindow(SelectableScreen screen, Presentable presentable, ScreenContentsEnum contents,
		SettingsModel settings) {
		super(ScreenHelper.getConfiguration(screen));
		setAutoRequestFocus(false);
		this.screen = screen;
		
		this.presentable = presentable;
		this.contents = contents;
		this.settings = settings;
		setIconImage(Toolkit.getDefaultToolkit().getImage(
			PresenterWindow.class.getResource("/org/zephyrsoft/sdb2/icon-16.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		backgroundColor = settings.get(SettingKey.BACKGROUND_COLOR, Color.class);
		setBackground(backgroundColor);
		contentPane.setBackground(backgroundColor);
		setContentPane(contentPane);
		
		// hide cursor above presentation display
		transparentCursor = getTransparentCursor();
		setCursor(transparentCursor);
		contentPane.setCursor(transparentCursor);
		
		// remove window decorations
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		// maximize window on indicated screen
		setBounds(ScreenHelper.getConfiguration(screen).getBounds());
		
		prepareContent();
	}
	
	private void prepareContent() {
		int topMargin = settings.get(SettingKey.TOP_MARGIN, Integer.class);
		int leftMargin = settings.get(SettingKey.LEFT_MARGIN, Integer.class);
		int rightMargin = settings.get(SettingKey.RIGHT_MARGIN, Integer.class);
		int bottomMargin = settings.get(SettingKey.BOTTOM_MARGIN, Integer.class);
		
		if (presentable.getSong() != null) {
			// determine WHAT to present and HOW to present it
			boolean showTitle = settings.get(SettingKey.SHOW_TITLE, Boolean.class).booleanValue();
			boolean showChords = (contents == ScreenContentsEnum.LYRICS_AND_CHORDS);
			Font titleFont = settings.get(SettingKey.TITLE_FONT, Font.class);
			Font lyricsFont = settings.get(SettingKey.LYRICS_FONT, Font.class);
			Font translationFont = settings.get(SettingKey.TRANSLATION_FONT, Font.class);
			Font copyrightFont = settings.get(SettingKey.COPYRIGHT_FONT, Font.class);
			int titleLyricsDistance = settings.get(SettingKey.DISTANCE_TITLE_TEXT, Integer.class);
			int lyricsCopyrightDistance = settings.get(SettingKey.DISTANCE_TEXT_COPYRIGHT, Integer.class);
			Color foregroundColor = settings.get(SettingKey.TEXT_COLOR, Color.class);
			
			// create a SongView to render the song
			songView = new SongView.Builder(presentable.getSong()).showTitle(showTitle).showChords(showChords)
				.titleFont(titleFont).lyricsFont(lyricsFont).translationFont(translationFont)
				.copyrightFont(copyrightFont).topMargin(topMargin).leftMargin(leftMargin).rightMargin(rightMargin)
				.bottomMargin(bottomMargin).titleLyricsDistance(titleLyricsDistance)
				.lyricsCopyrightDistance(lyricsCopyrightDistance).foregroundColor(foregroundColor)
				.backgroundColor(backgroundColor).build();
			songView.setOpaque(true);
			contentPane.add(songView, BorderLayout.CENTER);
		} else if (presentable.getImage() != null) {
			// display the image (fullscreen, but with margin)
			Image image = presentable.getImage();
			ImagePanel imagePanel = new ImagePanel(image);
			imagePanel.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
			contentPane.add(imagePanel, BorderLayout.CENTER);
		} else {
			// display a blank screen: only set the background color (already done)
		}
		// TODO if necessary: hide cursor above every child of the content pane
		
	}
	
	private static Cursor getTransparentCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
		return transparentCursor;
	}
	
	@Override
	public void showPresenter() {
		// ensure that the window is on the configured screen
		Rectangle targetCoordinates = ScreenHelper.getConfiguration(screen).getBounds();
		Point currentLocation = getLocation();
		Dimension currentSize = getSize();
		LOG.trace("presenter window for {} is at {}/{} with size {}x{}", screen.getDescription(),
			currentLocation.getX(), currentLocation.getY(), currentSize.getWidth(), currentSize.getHeight());
		
		if (!sameInt(targetCoordinates.getX(), currentLocation.getX())
			|| !sameInt(targetCoordinates.getY(), currentLocation.getY())
			|| !sameInt(targetCoordinates.getWidth(), currentSize.getWidth())
			|| !sameInt(targetCoordinates.getHeight(), currentSize.getHeight())) {
			setLocation((int) targetCoordinates.getX(), (int) targetCoordinates.getY());
			setSize((int) targetCoordinates.getWidth(), (int) targetCoordinates.getHeight());
			LOG.debug("presenter window for {} is now moved to {}/{} with size {}x{}", screen.getDescription(),
				targetCoordinates.getX(), targetCoordinates.getY(), targetCoordinates.getWidth(), targetCoordinates.getHeight());
		}
		
		// TODO fade in
		setVisible(true);
	}
	
	private boolean sameInt(double one, double two) {
		return (int) one == (int) two;
	}
	
	@Override
	public void hidePresenter() {
		// TODO fade out
		setVisible(false);
	}
	
	@Override
	public void disposePresenter() {
		LOG.debug("disposing presenter for {}", presentable);
		dispose();
	}
	
	@Override
	public void moveToPart(Integer part) {
		if (songView != null) {
			songView.moveToPart(part);
		} else {
			throw new IllegalStateException("it seems there is no song to display");
		}
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		if (songView != null) {
			songView.moveToLine(part, line);
		} else {
			throw new IllegalStateException("it seems there is no song to display");
		}
	}
	
	@Override
	public List<AddressablePart> getParts() {
		if (songView != null) {
			return songView.getParts();
		} else {
			throw new IllegalStateException("it seems there is no song to display");
		}
	}
}
