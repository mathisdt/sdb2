open module org.zephyrsoft.sdb2 {
	
	requires org.slf4j;
	requires org.apache.commons.lang3;
	requires org.apache.commons.text;
	requires com.google.common;
	requires spring.core;
	requires spring.context;
	requires spring.beans;
	requires togglz.core;
	
	requires args4j;
	requires lucene.core;
	requires poi;
	requires itextpdf;
	
	requires java.xml.bind;
	requires java.desktop;
	requires java.sql;
	requires java.net.http;
	requires com.google.gson;
	requires JFontChooser;
	requires timingframework.core;
	requires timingframework.swing;
	// for togglz:
	requires java.scripting;
	requires org.eclipse.paho.client.mqttv3;
	
}
