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
 * 
 * @author Mathis Dirksen-Thedens
 */
public abstract class KeyboardShortcut {
	private int keyCode = -1;
	private boolean shiftModifier = false;
	private boolean altModifier = false;
	private boolean ctrlModifier = false;
	
	public KeyboardShortcut(int keyCode, boolean shiftModifier, boolean altModifier, boolean ctrlModifier) {
		this.keyCode = keyCode;
		this.shiftModifier = shiftModifier;
		this.altModifier = altModifier;
		this.ctrlModifier = ctrlModifier;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public boolean usesShiftModifier() {
		return shiftModifier;
	}
	
	public boolean usesAltModifier() {
		return altModifier;
	}
	
	public boolean usesCtrlModifier() {
		return ctrlModifier;
	}
	
	public abstract void doAction();
}
