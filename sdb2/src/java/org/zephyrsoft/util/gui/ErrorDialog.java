package org.zephyrsoft.util.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

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
		setTitle("Error");
		setSize(400, 250);
		setLocationRelativeTo(parent);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				txtrAaa = new JTextArea();
				txtrAaa.setText("AAA");
				txtrAaa.setBorder(new EmptyBorder(15, 15, 15, 15));
				txtrAaa.setBackground(new Color(255, 255, 153));
				txtrAaa.setOpaque(true);
				txtrAaa.setFont(new Font("Monospaced", Font.PLAIN, 10));
				txtrAaa.setEditable(false);
				scrollPane.setViewportView(txtrAaa);
			}
		}
		{
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
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	public void setText(String t) {
		txtrAaa.setText(t);
		txtrAaa.setCaretPosition(0);
	}
	
}
