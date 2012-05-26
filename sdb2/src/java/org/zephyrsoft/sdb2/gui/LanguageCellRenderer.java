package org.zephyrsoft.sdb2.gui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.LanguageEnum;

/**
 * A {@link ListCellRenderer} for {@link LanguageEnum} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class LanguageCellRenderer implements ListCellRenderer<LanguageEnum> {
	
	private static final Logger LOG = LoggerFactory.getLogger(LanguageCellRenderer.class);
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends LanguageEnum> list, LanguageEnum value, int index,
		boolean isSelected, boolean cellHasFocus) {
		JLabel ret =
			(JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value == null) {
			ret.setText("");
		} else if (value == LanguageEnum.GERMAN) {
			ret.setText("German");
		} else if (value == LanguageEnum.ENGLISH) {
			ret.setText("English");
		} else if (value == LanguageEnum.MIXED) {
			ret.setText("Mixed");
		} else {
			LOG.warn("unknown language enum value: {}", value.toString());
			ret.setText(value.getLanguageAbbreviation());
		}
		return ret;
	}
}
