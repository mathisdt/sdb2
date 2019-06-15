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

/**
 * Template for keyboard shortcut definitions.
 */
public class KeyboardShortcut {
	private final int keyCode;
	private final Modifiers modifiers;
	private final Runnable action;
	
	public KeyboardShortcut(int keyCode, Modifiers modifiers, Runnable action) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
		this.action = action;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public boolean usesShiftModifier() {
		return modifiers.hasShift();
	}
	
	public boolean usesAltModifier() {
		return modifiers.hasAlt();
	}
	
	public boolean usesCtrlModifier() {
		return modifiers.hasCtrl();
	}
	
	public void doAction() {
		action.run();
	}
	
	public enum Modifiers {
		NONE(false, false, false),
		SHIFT(true, false, false),
		ALT(false, true, false),
		CTRL(false, false, true),
		SHIFT_ALT(true, true, false),
		CTRL_ALT(false, true, true),
		SHIFT_CTRL(true, false, true),
		SHIFT_ALT_CTRL(true, true, true);
		
		private final boolean shift;
		private final boolean alt;
		private final boolean ctrl;
		
		private Modifiers(boolean shift, boolean alt, boolean ctrl) {
			this.shift = shift;
			this.alt = alt;
			this.ctrl = ctrl;
		}
		
		public boolean hasShift() {
			return shift;
		}
		
		public boolean hasAlt() {
			return alt;
		}
		
		public boolean hasCtrl() {
			return ctrl;
		}
	}
}
