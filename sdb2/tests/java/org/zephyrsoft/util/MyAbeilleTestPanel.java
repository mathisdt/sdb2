package org.zephyrsoft.util;

import javax.swing.*;
import org.zephyrsoft.util.gui.*;

/**
 * Test class for the automatic field population. 
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MyAbeilleTestPanel extends AbeillePanel {
	private static final long serialVersionUID = 2085932072819350375L;
	
	@AutoPopulate
	private JLabel label1;
	@AutoPopulate
	private JButton button2;
	@AutoPopulate
	private JTextField textfield3;
	@AutoPopulate
	private JTextArea textarea4;
	
	public MyAbeilleTestPanel() {
		// verify that the fields are filled by printing out the contents
		System.out.println(label1);
		System.out.println(button2);
		System.out.println(textfield3);
		System.out.println(textarea4);
	}
	
	public static void main(String[] args) {
		MyAbeilleTestPanel testPanel = new MyAbeilleTestPanel();
		JFrame frame = new JFrame();
		frame.add(testPanel);
		frame.setVisible(true);
		frame.setSize(200, 300);
	}
}