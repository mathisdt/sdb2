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
package org.zephyrsoft.sdb2.gui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Manager for window-wide keyboard shortcuts.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class KeyboardShortcutManager implements KeyEventDispatcher {
	
	private Set<KeyboardShortcut> shortcuts = new HashSet<KeyboardShortcut>();
	
	/**
	 * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		for (KeyboardShortcut s : shortcuts) {
			if (e.isAltDown() == s.usesAltModifier() && e.isControlDown() == s.usesCtrlModifier()
				&& e.isShiftDown() == s.usesShiftModifier() && e.getKeyCode() == s.getKeyCode()
				&& e.paramString().startsWith("KEY_PRESSED")) {
				s.doAction();
				return true;
			}
		}
		return false;
	}
	
	public boolean add(KeyboardShortcut e) {
		return shortcuts.add(e);
	}
	
	public boolean remove(KeyboardShortcut o) {
		return shortcuts.remove(o);
	}
	
	public void clear() {
		shortcuts.clear();
	}
}
