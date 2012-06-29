package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.LanguageEnum;

/**
 * A {@link ListCellRenderer} for {@link LanguageEnum} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class FilterTypeCellRenderer implements ListCellRenderer<FilterTypeEnum> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FilterTypeCellRenderer.class);
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends FilterTypeEnum> list, FilterTypeEnum value,
		int index, boolean isSelected, boolean cellHasFocus) {
		JLabel ret =
			(JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value == null) {
			ret.setText("");
		} else if (value == FilterTypeEnum.ONLY_LYRICS) {
			ret.setText("only lyrics");
		} else if (value == FilterTypeEnum.ONLY_TITLE) {
			ret.setText("only title");
		} else if (value == FilterTypeEnum.TITLE_AND_FIRST_LYRICS_LINE) {
			ret.setText("title and first line of lyrics");
		} else if (value == FilterTypeEnum.TITLE_AND_LYRICS) {
			ret.setText("title and lyrics");
		} else {
			LOG.warn("unknown filter type enum value: {}", value.toString());
			ret.setText(value.name());
		}
		return ret;
	}
}
