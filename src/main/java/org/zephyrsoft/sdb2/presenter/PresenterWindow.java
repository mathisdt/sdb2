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
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
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
	
	private JPanel contentPane;
	private SongView songView;
	
	private final SettingsModel settings;
	private final ScreenContentsEnum contents;
	private final Presentable presentable;
	
	private Cursor transparentCursor;
	
	private Color backgroundColor;
	
	public PresenterWindow(GraphicsDevice screen, Presentable presentable, ScreenContentsEnum contents,
		SettingsModel settings) {
		super(screen.getDefaultConfiguration());
		this.presentable = presentable;
		this.contents = contents;
		this.settings = settings;
		setIconImage(Toolkit.getDefaultToolkit().getImage(
			PresenterWindow.class.getResource("/org/zephyrsoft/sdb2/icon-16.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		backgroundColor = settings.getColor(SettingKey.BACKGROUND_COLOR);
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
		setBounds(screen.getDefaultConfiguration().getBounds());
		
		prepareContent();
	}
	
	private void prepareContent() {
		int topMargin = settings.getInteger(SettingKey.TOP_MARGIN);
		int leftMargin = settings.getInteger(SettingKey.LEFT_MARGIN);
		int rightMargin = settings.getInteger(SettingKey.RIGHT_MARGIN);
		int bottomMargin = settings.getInteger(SettingKey.BOTTOM_MARGIN);
		
		if (presentable.getSong() != null) {
			// determine WHAT to present and HOW to present it
			boolean showTitle = settings.getBoolean(SettingKey.SHOW_TITLE).booleanValue();
			boolean showChords = (contents == ScreenContentsEnum.LYRICS_AND_CHORDS);
			Font titleFont = settings.getFont(SettingKey.TITLE_FONT);
			Font lyricsFont = settings.getFont(SettingKey.LYRICS_FONT);
			Font translationFont = settings.getFont(SettingKey.TRANSLATION_FONT);
			Font copyrightFont = settings.getFont(SettingKey.COPYRIGHT_FONT);
			int titleLyricsDistance = settings.getInteger(SettingKey.DISTANCE_TITLE_TEXT);
			int lyricsCopyrightDistance = settings.getInteger(SettingKey.DISTANCE_TEXT_COPYRIGHT);
			Color foregroundColor = settings.getColor(SettingKey.TEXT_COLOR);
			
			// create a SongView to render the song
			songView =
				new SongView.Builder(presentable.getSong()).showTitle(showTitle).showChords(showChords)
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
		Cursor transparentCursor =
			Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
		return transparentCursor;
	}
	
	@Override
	public void showPresenter() {
		// TODO fade in
		setVisible(true);
	}
	
	@Override
	public void hidePresenter() {
		// TODO fade out
		setVisible(false);
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
