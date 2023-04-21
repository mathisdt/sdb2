/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.zephyrsoft.sdb2.presenter.NamedSongPresentationPosition;

/**
 * A {@link ListCellRenderer} for song text lines.
 */
public class TextLineCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = -9042262841850129416L;
	
	private Component reference;
	
	public TextLineCellRenderer(Component reference) {
		this.reference = reference;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
		Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		Component rendered = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if (isSelected
			&& value instanceof NamedSongPresentationPosition nssp
			&& nssp.getPartIndex() == null
			&& nssp.getLineIndex() == null) {
			// signal to user that this line is not selectable
			rendered.setBackground(list.getBackground());
		}
		
		rendered.setSize((int) Math.min(rendered.getSize().getWidth(), reference.getWidth()), (int) rendered.getSize().getHeight());
		
		return rendered;
	}
}
