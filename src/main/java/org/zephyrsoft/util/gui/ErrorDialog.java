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
package org.zephyrsoft.util.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.zephyrsoft.sdb2.gui.MainWindow;

/**
 * A dialog for displaying fatal errors (such as uncaught exceptions).
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ErrorDialog extends JDialog {
	
	private static final long serialVersionUID = 2294895894050002050L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextArea txtrAaa;
	
	public ErrorDialog(Component parent) {
		setIconImages(MainWindow.getIconsFromResources(getClass()));
		setTitle("Error");
		setSize(400, 250);
		if (parent != null) {
			setLocationRelativeTo(parent);
		} else {
			Rectangle defaultScreenRectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
				.getBounds();
			setLocation((int) (defaultScreenRectangle.getWidth() / 2 - getSize().getWidth() / 2),
				(int) (defaultScreenRectangle.getHeight() / 2 - getSize().getHeight() / 2));
		}
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		{
			txtrAaa = new JTextArea();
			txtrAaa.setText("AAA");
			txtrAaa.setBorder(new EmptyBorder(15, 15, 15, 15));
			txtrAaa.setBackground(new Color(255, 255, 153));
			txtrAaa.setOpaque(true);
			txtrAaa.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
			txtrAaa.setEditable(false);
			scrollPane.setViewportView(txtrAaa);
		}
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		{
			JButton okButton = new JButton("Close");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ErrorDialog.this.setVisible(false);
					ErrorDialog.this.dispose();
				}
			});
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
		}
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	public void setText(String t) {
		txtrAaa.setText(t);
		txtrAaa.setCaretPosition(0);
	}
	
	/**
	 * Open an error dialog.
	 * 
	 * @param parent
	 *            the parent component
	 * @param text
	 *            the message to display
	 */
	public static void openDialog(Component parent, String text) {
		ErrorDialog dialog = new ErrorDialog(parent);
		dialog.setText(text);
		dialog.setVisible(true);
	}
	
	/**
	 * Open an error dialog.
	 * 
	 * @param parent
	 *            the parent component
	 * @param text
	 *            the message to display
	 */
	public static void openDialogBlocking(Component parent, String text) {
		ErrorDialog dialog = new ErrorDialog(parent);
		dialog.setText(text);
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setVisible(true);
		
	}
	
}
