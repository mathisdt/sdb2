/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Component;
import java.awt.GraphicsDevice;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.zephyrsoft.sdb2.model.SelectableDisplay;

/**
 * A {@link ListCellRenderer} for {@link GraphicsDevice} values.
 */
public class ScreenDisplayCellRenderer implements ListCellRenderer<SelectableDisplay> {
	
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends SelectableDisplay> list, SelectableDisplay value,
		int index, boolean isSelected, boolean cellHasFocus) {
		JLabel ret = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
			cellHasFocus);
		if (value == null) {
			ret.setText("hidden");
		} else {
			ret.setText(value.getDescription());
		}
		return ret;
	}
}
