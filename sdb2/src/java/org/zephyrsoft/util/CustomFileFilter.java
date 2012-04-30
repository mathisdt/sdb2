package org.zephyrsoft.util;

import java.io.File;

/**
 * A filter that accepts all directories (with any extenion or none at all) and all files with the specified
 * extension(s).
 * 
 * @author Mathis Dirksen-Thedens
 */
public class CustomFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
	
	private String[] extension = new String[] {};
	private String description = "";
	
	public CustomFileFilter(String description, String... extension) {
		this.extension = extension;
		this.description = description;
	}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		for (int i = 0; i < extension.length; i++) {
			if (f.getName().toLowerCase().endsWith(extension[i])) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
}
