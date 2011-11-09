package org.zephyrsoft.sdb2.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
