package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Component;
import java.awt.GraphicsDevice;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;

/**
 * A {@link ListCellRenderer} for {@link GraphicsDevice} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ScreenDisplayCellRenderer implements ListCellRenderer<GraphicsDevice> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ScreenDisplayCellRenderer.class);
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends GraphicsDevice> list, GraphicsDevice value,
		int index, boolean isSelected, boolean cellHasFocus) {
		JLabel ret =
			(JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value == null) {
			ret.setText("hidden");
		} else {
			ret.setText(ScreenHelper.getScreenId(value));
		}
		return ret;
	}
}
