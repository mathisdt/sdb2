package org.zephyrsoft.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Helper class for using features of JARs, e.g. reading from the manifest.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class JarTools {
	
	private JarTools() {
		// this class is not intended for instantiation
	}
	
	public static String getAttributeFromManifest(String attributeName) {
		String ret = null;
		InputStream manifestStream = JarTools.class.getResourceAsStream("META-INF/MANIFEST.MF");
		if (manifestStream == null) {
			// try again using a "root" slash
			manifestStream = JarTools.class.getResourceAsStream("/META-INF/MANIFEST.MF");
		}
		try {
			Manifest manifest = new Manifest(manifestStream);
			Attributes attributes = manifest.getMainAttributes();
			ret = attributes.getValue(attributeName);
		} catch (IOException e) {
			// swallow exception here, just return null
		}
		
		return ret;
	}
	
}
