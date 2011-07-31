package org.zephyrsoft.util.gui;

import java.lang.reflect.*;
import javax.swing.*;
import com.jeta.forms.components.panel.*;

/**
 * Helper class for easy implementation of Abeille Forms.<br/>
 * <br/>
 * To use this, you have to:<br/>
 * 1. create an Abeille form AND<br/>
 * 2. create a class with a package and name that matches the package and name you chose for your new form, inheriting from this class AND<br/>
 * 3. create fields with names and types that match the names and types you chose in Abeille Forms Designer AND<br/>
 * 4. annotate the fields you want to have automatically populated with {@code @AutoPopulate}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public abstract class AbeilleForm extends JFrame {
	
	private static final long serialVersionUID = -7153324343867137784L;
	
	private FormPanel panel;
	
	public AbeilleForm() {
		super();
		Class<?> clazz = getClass();
		String clazzName = clazz.getCanonicalName();
		String pathFromclazzName = clazzName.replaceAll("\\.", "/");
		panel = new FormPanel(pathFromclazzName + ".jfrm");
		add(panel);
		autoPopulateFields(clazz);
	}
	
	private void autoPopulateFields(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				if (field.isAnnotationPresent(AutoPopulate.class)) {
					field.setAccessible(true);
					if (field.getType().equals(JLabel.class)) {
						field.set(this, panel.getLabel(field.getName()));
					} else if (field.getType().equals(JButton.class)) {
						field.set(this, (JButton)panel.getButton(field.getName()));
					} else if (field.getType().equals(JTextField.class)) {
						field.set(this, panel.getTextField(field.getName()));
					} else if (field.getType().equals(JTextArea.class)) {
						field.set(this, (JTextArea)panel.getTextComponent(field.getName()));
					} else {
						throw new UnsupportedOperationException();
					}
				}
			} catch (IllegalAccessException iae) {
				// TODO logging
			}
		}
	}
}
