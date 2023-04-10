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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
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
import org.zephyrsoft.sdb2.model.SelectableDisplay;
import org.zephyrsoft.sdb2.model.VirtualScreen;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.settings.VirtualScreenSettingsModel;

/**
 * The presentation display for the lyrics.
 */
public class PresenterWindow extends JFrame implements Presenter {
	private static final long serialVersionUID = -2390663756699128439L;
	
	private static final Logger LOG = LoggerFactory.getLogger(PresenterWindow.class);
	
	private final JPanel contentPane;
	private SongView songView;
	
	private Animator fader;
	
	private final VirtualScreen virtualScreen;
	private final SettingsModel settings;
	private final SelectableDisplay screen;
	private VirtualScreenSettingsModel screenSettings;
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
		
		// remove window decorations
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		
		calculateScreenSize();
		
		setBounds(screenSize);
		
		fader = createFader();
		
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		Color backgroundColor = virtualScreen.getBackgroundColor(settings);
		setBackgroundColor(backgroundColor);
		// hide cursor above presentation display
		contentPane.setCursor(transparentCursor);
		
		TranslucentPanel glassPane = new TranslucentPanel(backgroundColor);
		setGlassPane(glassPane);
		glassPane.setVisible(true);
		glassPane.setSize(getWidth(), getHeight());
		
		setContentPane(contentPane);
		
		setContent(presentable);
	}
	
	private void calculateScreenSize() {
		GraphicsConfiguration graphicsConfiguration = ScreenHelper.getConfiguration(screen);
		if (graphicsConfiguration == null) {
			throw new IllegalStateException("please check cables and configuration - could not use " + screen.getDescription());
		}
		Rectangle screenSizeUntransformed = graphicsConfiguration.getBounds();
		Rectangle2D screenSizeTransformed = graphicsConfiguration.getDefaultTransform().createTransformedShape(screenSizeUntransformed).getBounds2D();
		int width = (int) screenSizeTransformed.getMaxX() - (int) screenSizeTransformed.getMinX();
		int height = (int) screenSizeTransformed.getMaxY() - (int) screenSizeTransformed.getMinY();
		screenSize = new Rectangle((int) screenSizeTransformed.getMinX(), (int) screenSizeTransformed.getMinY(), width, height);
		// screenSize = graphicsConfiguration.getBounds();
	}
	
	private void setBackgroundColor(Color backgroundColor) {
		if (!Objects.equals(backgroundColor, getBackground())) {
			setBackground(backgroundColor);
		}
		if (!Objects.equals(backgroundColor, contentPane.getBackground())) {
			contentPane.setBackground(backgroundColor);
		}
	}
	
	public boolean metadataMatches(SelectableDisplay otherScreen, VirtualScreen otherVirtualScreen) {
		return screen.equals(otherScreen) && virtualScreen.equals(otherVirtualScreen);
	}
	
	public boolean screenSizeMatches() {
		calculateScreenSize();
		return getBounds().equals(screenSize);
	}
	
	@Override
	public void setContent(Presentable presentable) {
		if (Objects.equals(presentable, this.presentable) && songView.getScrollPosition() == 0 && noSettingsWereChanged()) {
			return;
		}
		
		this.presentable = presentable;
		
		if (contentPane.getComponentCount() > 0) {
			fadeOut();
		} else {
			fadeOutAbruptly();
		}
		
		controller.contentChange(() -> {
			contentPane.removeAll();
			
			screenSettings = VirtualScreenSettingsModel.of(settings, virtualScreen);
			setBackgroundColor(screenSettings.getBackgroundColor());
			if (getGlassPane() instanceof TranslucentPanel tp
				&& !Objects.equals(screenSettings.getBackgroundColor(), tp.getBaseColor())) {
				tp.setBaseColor(screenSettings.getBackgroundColor());
			}
			
			if (presentable.getSong() != null) {
				// create a SongView to render the song
				songView = new SongView.Builder(presentable.getSong())
					.showTitle(screenSettings.isShowTitle())
					.showTranslation(screenSettings.isShowTranslation())
					.showChords(screenSettings.isShowChords())
					.titleFont(screenSettings.getTitleFont())
					.lyricsFont(screenSettings.getLyricsFont())
					.translationFont(screenSettings.getTranslationFont())
					.copyrightFont(screenSettings.getCopyrightFont())
					.topMargin(screenSettings.getTopMargin())
					.leftMargin(screenSettings.getLeftMargin())
					.rightMargin(screenSettings.getRightMargin())
					.bottomMargin(screenSettings.getBottomMargin())
					.titleLyricsDistance(screenSettings.getTitleLyricsDistance())
					.lyricsCopyrightDistance(screenSettings.getLyricsCopyrightDistance())
					.foregroundColor(screenSettings.getForegroundColor())
					.backgroundColor(screenSettings.getBackgroundColor())
					.minimalScrolling(screenSettings.isMinimalScrolling())
					.build();
				contentPane.add(songView, BorderLayout.CENTER);
				
				if (screenSettings.isShowChordSequence()) {
					contentPane.add(new ChordSequenceView(presentable.getSong(), screenSettings.getChordSequenceFont(), screenSettings
						.getForegroundColor(), screenSettings.getBackgroundColor()),
						BorderLayout.SOUTH);
				}
				
			} else if (presentable.getImage() != null) {
				// display the image (fullscreen, but with margin)
				String imageFile = presentable.getImage();
				ImageIcon imageIcon = new ImageIcon(imageFile);
				Image image = imageIcon.getImage();
				int originalWidth = image.getWidth(null);
				int originalHeight = image.getHeight(null);
				double factor = Math.min((screenSize.getWidth() - screenSettings.getLeftMargin() - screenSettings.getRightMargin()) / originalWidth,
					(screenSize.getHeight() - screenSettings.getTopMargin() - screenSettings.getBottomMargin()) / originalHeight);
				image = image.getScaledInstance((int) (originalWidth * factor), (int) (originalHeight * factor), Image.SCALE_FAST);
				JLabel imageComponent = new JLabel(new ImageIcon(image));
				imageComponent.setBorder(BorderFactory.createEmptyBorder(screenSettings.getTopMargin(), screenSettings.getLeftMargin(), screenSettings
					.getBottomMargin(), screenSettings.getRightMargin()));
				contentPane.add(imageComponent, BorderLayout.CENTER);
			} else {
				// display a blank screen, remove all content: already done
			}
			validate();
		});
		
		toFront();
		
		fadeIn();
		
		// TODO if necessary: hide cursor above every child of the content pane
	}
	
	private boolean noSettingsWereChanged() {
		VirtualScreenSettingsModel screenSettingsToCompare = VirtualScreenSettingsModel.of(settings, virtualScreen);
		return screenSettingsToCompare.equals(screenSettings);
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
			if (fader.getDuration() != settings.get(SettingKey.FADE_TIME, Integer.class).longValue()) {
				fader = createFader();
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
		return new Animator.Builder().setDuration(settings.get(SettingKey.FADE_TIME, Integer.class), TimeUnit.MILLISECONDS).build();
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
