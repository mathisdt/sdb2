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
import java.awt.Cursor;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SettingKey;
import org.zephyrsoft.sdb2.model.SettingsModel;

/**
 * The presentation display for the lyrics.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PresenterWindow extends JFrame implements Presenter {
	
	private static final long serialVersionUID = -2390663756699128439L;
	
	private JPanel contentPane;
	
	private final SettingsModel settings;
	private final ScreenContentsEnum contents;
	private final Presentable presentable;
	
	/**
	 * Create the frame.
	 */
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
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// set cursor to transparent in this window
		Cursor transparentCursor = getTransparentCursor();
		setCursor(transparentCursor);
		contentPane.setCursor(transparentCursor);
		// TODO set cursor on EVERY child of the content pane
		
		// remove window decorations
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		// maximize window on indicated screen
		setBounds(screen.getDefaultConfiguration().getBounds());
//		Dimension dim = new Dimension(screen.getDisplayMode().getWidth(), screen.getDisplayMode().getHeight());
//		setSize(dim);
		
		prepareContent();
	}
	
	private void prepareContent() {
		// TODO determine WHAT to present (title? chords?) and HOW to present it (fonts, margins, colors)
		boolean showTitle = settings.getBoolean(SettingKey.SHOW_TITLE).booleanValue();
		
	}
	
	private static Cursor getTransparentCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor =
			Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
		return transparentCursor;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#moveToPart(java.lang.Integer)
	 */
	@Override
	public void moveToPart(Integer part) {
		// TODO
		
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#showPresenter()
	 */
	@Override
	public void showPresenter() {
		// TODO fade in
		setVisible(true);
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#hidePresenter()
	 */
	@Override
	public void hidePresenter() {
		// TODO fade out
		setVisible(false);
	}
	
}
