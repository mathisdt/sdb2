package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;

/**
 * A {@link ListCellRenderer} for {@link ScreenContentsEnum} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ScreenContentsCellRenderer implements ListCellRenderer<ScreenContentsEnum> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ScreenContentsCellRenderer.class);
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends ScreenContentsEnum> list, ScreenContentsEnum value,
		int index, boolean isSelected, boolean cellHasFocus) {
		JLabel ret =
			(JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value == null) {
			ret.setText("");
		} else if (value == ScreenContentsEnum.ONLY_LYRICS) {
			ret.setText("only lyrics");
		} else if (value == ScreenContentsEnum.LYRICS_AND_CHORDS) {
			ret.setText("lyrics and chords");
		} else {
			LOG.warn("unknown screen contents enum value: {}", value.toString());
			ret.setText(value.name());
		}
		return ret;
	}
}
