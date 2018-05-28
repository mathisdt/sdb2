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
package org.zephyrsoft.sdb2.util;

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
