package org.zephyrsoft.sdb2.gui;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.zephyrsoft.sdb2.model.Song;

public class SongCellRenderer extends JLabel implements ListCellRenderer<Song> {
	
	private static final long serialVersionUID = -9042262843850129406L;
	
	public SongCellRenderer() {
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Song> list, Song value, int index,
		boolean isSelected, boolean cellHasFocus) {
		if (!(value instanceof Song)) {
			throw new IllegalArgumentException("value is not a Song instance");
		}
		Song song = value;
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
