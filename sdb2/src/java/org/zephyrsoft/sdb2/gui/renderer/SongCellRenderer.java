package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.zephyrsoft.sdb2.gui.SongCell;
import org.zephyrsoft.sdb2.model.Song;

/**
 * A {@link ListCellRenderer} for {@link Song} values.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class SongCellRenderer implements ListCellRenderer<Song> {
	
	private static final long serialVersionUID = -9042262843850129406L;
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Song> list, Song value, int index,
		boolean isSelected, boolean cellHasFocus) {
		
		SongCell ret = new SongCell(30);
		ret.setSongTitle(value.getTitle());
		ret.setFirstLine(value.getLyricsFirstLine());
		
		if (isSelected) {
			ret.setForeground(list.getSelectionForeground());
			ret.setBackground(list.getSelectionBackground());
		} else {
			ret.setForeground(list.getForeground());
			if (index % 2 == 0) {
				ret.setBackground(Color.WHITE);
			} else {
				ret.setBackground(new Color(230, 230, 230));
			}
		}
		
		return ret;
	}
}
