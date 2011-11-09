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
