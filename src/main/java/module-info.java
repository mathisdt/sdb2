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
	requires org.apache.lucene.core;
	requires org.apache.lucene.analysis.common;
	requires org.apache.poi.poi;
	
	requires kernel;
	requires io;
	requires layout;
	
	requires jakarta.xml.bind;
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
	requires org.bitbucket.cowwoc.diffmatchpatch;
	
}
