package org.zephyrsoft.sdb2.gui;

import java.awt.*;
import javax.swing.*;
import org.zephyrsoft.sdb2.model.*;

public class SongCellRenderer extends JLabel implements ListCellRenderer {
	
	private static final long serialVersionUID = -9042262843850129406L;
	
	public SongCellRenderer() {
		setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
		boolean cellHasFocus) {
		if (!(value instanceof Song)) {
			throw new IllegalArgumentException("value is not a Song instance");
		}
		Song song = (Song) value;
		String title = song.getTitle();
		if (title == null || title.trim().isEmpty()) {
			setText(" ");
		} else {
			setText(title);
		}
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		return this;
	}
}
