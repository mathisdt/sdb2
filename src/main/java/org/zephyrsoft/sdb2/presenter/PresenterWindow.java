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
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.PropertySetter;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.core.animation.timing.interpolators.LinearInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SelectableDisplay;
import org.zephyrsoft.sdb2.model.VirtualScreen;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;

/**
 * The presentation display for the lyrics.
 */
public class PresenterWindow extends JFrame implements Presenter {
	private static final long serialVersionUID = -2390663756699128439L;
	
	private static final Logger LOG = LoggerFactory.getLogger(PresenterWindow.class);
	
	private final JPanel contentPane;
	private SongView songView;
	
	private Animator fader = createFader();
	
	private final VirtualScreen virtualScreen;
	private final SettingsModel settings;
	private final SelectableDisplay screen;
	private Presentable presentable;
	private MainController controller;
	
	private Cursor transparentCursor;
	
	private Rectangle screenSize;
	
	public PresenterWindow(SelectableDisplay screen, Presentable presentable,
		VirtualScreen virtualScreen, SettingsModel settings, MainController controller) {
		super(ScreenHelper.getConfiguration(screen));
		this.controller = controller;
		setAutoRequestFocus(false);
		this.screen = screen;
		
		this.virtualScreen = virtualScreen;
		this.settings = settings;
		setIconImage(Toolkit.getDefaultToolkit().getImage(
			PresenterWindow.class.getResource("/org/zephyrsoft/sdb2/icon-16.png")));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// hide cursor above presentation display
		transparentCursor = getTransparentCursor();
		setCursor(transparentCursor);
		
		GraphicsConfiguration graphicsConfiguration = ScreenHelper.getConfiguration(screen);
		if (graphicsConfiguration == null) {
			throw new IllegalStateException("please check cables and configuration - could not use " + screen.getDescription());
		}
		
		// remove window decorations
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		screenSize = graphicsConfiguration.getBounds();
		setBounds(screenSize);
		
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		Color backgroundColor = setBackgroundColor(virtualScreen, settings);
		// hide cursor above presentation display
		contentPane.setCursor(transparentCursor);
		
		TranslucentPanel glassPane = new TranslucentPanel(backgroundColor);
		setGlassPane(glassPane);
		glassPane.setVisible(true);
		glassPane.setSize(getWidth(), getHeight());
		
		setContentPane(contentPane);
		
		setContent(presentable);
	}
	
	private Color setBackgroundColor(VirtualScreen virtualScreen, SettingsModel settings) {
		Color backgroundColor = virtualScreen.getBackgroundColor(settings);
		if (!Objects.equals(backgroundColor, getBackground())) {
			setBackground(backgroundColor);
		}
		if (!Objects.equals(backgroundColor, contentPane.getBackground())) {
			contentPane.setBackground(backgroundColor);
		}
		return backgroundColor;
	}
	
	public boolean metadataMatches(SelectableDisplay otherScreen, VirtualScreen otherVirtualScreen) {
		return screen.equals(otherScreen) && virtualScreen.equals(otherVirtualScreen);
	}
	
	@Override
	public void setContent(Presentable presentable) {
		this.presentable = presentable;
		
		if (contentPane.getComponentCount() > 0) {
			fadeOut();
		} else {
			fadeOutAbruptly();
		}
		
		controller.contentChange(() -> {
			contentPane.removeAll();
			
			Color backgroundColor = setBackgroundColor(virtualScreen, settings);
			if (getGlassPane() instanceof TranslucentPanel
				&& !Objects.equals(backgroundColor, ((TranslucentPanel) getGlassPane()).getBaseColor())) {
				((TranslucentPanel) getGlassPane()).setBaseColor(backgroundColor);
			}
			
			int topMargin = settings.get(SettingKey.TOP_MARGIN, Integer.class);
			int leftMargin = settings.get(SettingKey.LEFT_MARGIN, Integer.class);
			int rightMargin = settings.get(SettingKey.RIGHT_MARGIN, Integer.class);
			int bottomMargin = settings.get(SettingKey.BOTTOM_MARGIN, Integer.class);
			
			if (presentable.getSong() != null) {
				ScreenContentsEnum contents = virtualScreen.getScreenContents(settings);
				
				// determine WHAT to present and HOW to present it
				boolean showTitle = settings.get(SettingKey.SHOW_TITLE, Boolean.class).booleanValue();
				boolean showTranslation = contents.shouldShowTranslation();
				boolean showChords = contents.shouldShowChords();
				Font titleFont = virtualScreen.getTitleFont(settings);
				Font lyricsFont = virtualScreen.getLyricsFont(settings);
				Font chordSequenceFont = virtualScreen.getChordSequenceFont(settings);
				Font translationFont = virtualScreen.getTranslationFont(settings);
				Font copyrightFont = virtualScreen.getCopyrightFont(settings);
				int titleLyricsDistance = settings.get(SettingKey.DISTANCE_TITLE_TEXT, Integer.class);
				int lyricsCopyrightDistance = settings.get(SettingKey.DISTANCE_TEXT_COPYRIGHT, Integer.class);
				Color foregroundColor = virtualScreen.getTextColor(settings);
				boolean minimalScrolling = virtualScreen.getMinimalScrolling(settings);
				
				// create a SongView to render the song
				songView = new SongView.Builder(presentable.getSong())
					.showTitle(showTitle).showTranslation(showTranslation).showChords(showChords)
					.titleFont(titleFont).lyricsFont(lyricsFont)
					.translationFont(translationFont).copyrightFont(copyrightFont).topMargin(topMargin)
					.leftMargin(leftMargin).rightMargin(rightMargin).bottomMargin(bottomMargin)
					.titleLyricsDistance(titleLyricsDistance).lyricsCopyrightDistance(lyricsCopyrightDistance)
					.foregroundColor(foregroundColor).backgroundColor(backgroundColor)
					.minimalScrolling(minimalScrolling).build();
				contentPane.add(songView, BorderLayout.CENTER);
				
				if (contents.shouldShowChordSequence()) {
					contentPane.add(new ChordSequenceView(presentable.getSong(), chordSequenceFont, foregroundColor, backgroundColor),
						BorderLayout.SOUTH);
				}
				
			} else if (presentable.getImage() != null) {
				// display the image (fullscreen, but with margin)
				String imageFile = presentable.getImage();
				ImageIcon imageIcon = new ImageIcon(imageFile);
				Image image = imageIcon.getImage();
				int originalWidth = image.getWidth(null);
				int originalHeight = image.getHeight(null);
				double factor = Math.min((screenSize.getWidth() - leftMargin - rightMargin) / originalWidth,
					(screenSize.getHeight() - topMargin - bottomMargin) / originalHeight);
				image = image.getScaledInstance((int) (originalWidth * factor), (int) (originalHeight * factor), Image.SCALE_FAST);
				JLabel imageComponent = new JLabel(new ImageIcon(image));
				imageComponent.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
				contentPane.add(imageComponent, BorderLayout.CENTER);
			} else {
				// display a blank screen, remove all content: already done
			}
			validate();
		});
		
		fadeIn();
		
		// TODO if necessary: hide cursor above every child of the content pane
	}
	
	private void fadeIn() {
		fadeTo(0);
	}
	
	private void fadeOut() {
		fadeTo(255);
	}
	
	private void fadeTo(int alpha) {
		controller.contentChange(() -> {
			TimingTargetAdapter target = PropertySetter.getTargetTo((TranslucentPanel) getGlassPane(), "alpha",
				LinearInterpolator.getInstance(), alpha);
			if (fader.isRunning()) {
				try {
					fader.await();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			fader.addTarget(target);
			fader.start();
			try {
				fader.await();
			} catch (InterruptedException e) {
				// do nothing
			}
		});
	}
	
	private void fadeOutAbruptly() {
		controller.contentChange(() -> ((TranslucentPanel) getGlassPane()).setAlpha(255));
	}
	
	private Animator createFader() {
		return new Animator.Builder().setDuration(75, TimeUnit.MILLISECONDS).build();
	}
	
	private static Cursor getTransparentCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
		return transparentCursor;
	}
	
	@Override
	public void showPresenter() {
		GraphicsConfiguration graphicsConfiguration = ScreenHelper.getConfiguration(screen);
		if (graphicsConfiguration == null) {
			throw new IllegalStateException("please check cables and configuration - could not use " + screen.getDescription());
		}
		// ensure that the window is on the configured screen
		Rectangle targetCoordinates = graphicsConfiguration.getBounds();
		Point currentLocation = getLocation();
		Dimension currentSize = getSize();
		LOG.debug("presenter window for {} is at {}/{} with size {}x{}", screen.getDescription(),
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
		
		setVisible(true);
	}
	
	private boolean sameInt(double one, double two) {
		return (int) one == (int) two;
	}
	
	@Override
	public void hidePresenter() {
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
