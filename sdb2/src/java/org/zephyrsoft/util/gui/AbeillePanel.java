package org.zephyrsoft.util.gui;

import java.lang.reflect.*;
import javax.swing.*;
import com.jeta.forms.components.panel.*;
import com.jeta.forms.gui.common.*;
import org.slf4j.*;

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
public abstract class AbeillePanel extends JPanel {
	
	private static final long serialVersionUID = -7153324343867137784L;
	
	protected final Logger LOG;
	
	private FormPanel panel;
	
	public AbeillePanel() {
		super();
		Class<?> clazz = getClass();
		LOG = LoggerFactory.getLogger(clazz);
		String clazzName = clazz.getSimpleName();
		try {
			panel = new FormPanel(clazz.getResourceAsStream(clazzName + ".jfrm"));
			add(panel);
			autoPopulateFields(clazz);
		} catch (FormException e) {
			LOG.error("could not load form layout", e);
		}
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
