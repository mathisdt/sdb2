package org.zephyrsoft.sdb2.presenter;

import java.util.List;

/**
 * Something that can be moved (vertically).
 * 
 * @author Mathis Dirksen-Thedens
 */
public interface Movable {
	
	/**
	 * Get the parts of this movable object so a specific part can be targeted.
	 */
	List<String> getParts();
	
}
