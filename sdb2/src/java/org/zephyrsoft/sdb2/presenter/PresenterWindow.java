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

/**
 * @author Mathis Dirksen-Thedens
 */
public class PresenterWindow extends JFrame {
	
	private static final long serialVersionUID = -2390663756699128439L;
	
	private JPanel contentPane;
	
	/**
	 * Create the frame.
	 */
	public PresenterWindow(GraphicsDevice screen) {
		super(screen.getDefaultConfiguration());
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
	}
	
	private static Cursor getTransparentCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor =
			Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
		return transparentCursor;
	}
	
}
