/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.zephyrsoft.sdb2.util.VersionTools;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

/**
 * Startup class for SDBv2.
 */
public final class Start {
	
	public static void main(String[] args) {
		new Start(args);
	}
	
	@SuppressWarnings("resource")
	private Start(String[] args) {
		Options options = Options.getInstance();
		
		// parse command line arguments
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			options.setHelp(true);
		}
		
		if (StringUtils.isNotBlank(options.getPropertiesFile())) {
			File propsFile = new File(options.getPropertiesFile());
			if (propsFile.exists() && propsFile.isFile() && propsFile.canRead()) {
				Properties props = new Properties();
				try (FileReader reader = new FileReader(propsFile)) {
					props.load(reader);
				} catch (Exception e) {
					System.err.println("problem while reading properties file: " + e.getMessage());
				}
				options.addMissingValuesFrom(props);
			}
		}
		
		// this is not a class-wide static logger and it's created here (after the "parser.parseArgument()" call)
		// because the logging system has to be initialized *after* parsing the arguments/properties into the Options
		// instance or else the log directory command line option could never have any effect, it would be too late
		Logger log = LoggerFactory.getLogger(Start.class);
		log.debug("starting application");
		
		if (options.isHelp()) {
			System.err.println("The available options are:");
			parser.printUsage(System.err);
		} else {
			try {
				if (options.getLanguage() != null) {
					System.setProperty("user.language", options.getLanguage());
					Locale.setDefault(Locale.forLanguageTag(options.getLanguage()));
				}
				if (options.getCountry() != null) {
					System.setProperty("user.country", options.getCountry());
					Locale.setDefault(Locale.forLanguageTag(System.getProperty("user.language") + "-" + options.getCountry()));
				}
				if (options.getTimezone() != null) {
					System.setProperty("user.timezone", options.getTimezone());
					TimeZone.setDefault(TimeZone.getTimeZone(options.getTimezone()));
				}
				
				log.debug("default file encoding is {}", Charset.defaultCharset().displayName());
				log.debug("default time zone is {}", ZoneId.systemDefault().getId());
				log.debug("default locale is {}", Locale.getDefault());
				log.debug("application version is {}", VersionTools.getCurrent());
				log.debug("application commit hash is {}", VersionTools.getGitCommitHash());

				// TODO remake startup process -> https://auth0.com/blog/spring-5-embedded-tomcat-8-gradle-tutorial/
				log.debug("loading application context");
				AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
				context.register(SpringConfiguration.class);
				context.refresh();

				if (options.getSplitMode() == Options.SplitMode.SERVER) {
					log.debug("starting API server");

					// TODO make port configurable
//					Tomcat tomcat = new Tomcat();
//					tomcat.setBaseDir(createTempDir(8899));
//					tomcat.setPort(8899);
//					String appBase = ".";
//					tomcat.getHost().setAppBase(appBase);
//					tomcat.addWebapp("", appBase);
//					tomcat.start();
//					tomcat.getServer().await();

					DispatcherServlet servlet = new DispatcherServlet(context);
					ServletRegistration.Dynamic registration = context.getBean(ServletContext.class).addServlet("api", servlet);
					registration.setLoadOnStartup(1);
					registration.addMapping("/*");
				} else {
					log.debug("not starting API (not configured as split mode server)");
				}
			} catch (Exception e) {
				log.error("problem while starting up the application", e);
				ErrorDialog.openDialogBlocking(null, "There was a problem while starting the Song Database:\n\n"
					+ e.getMessage()
					+ "\n\nThis is a fatal error, exiting.\n"
					+ "Please see the log file for more details.");
				System.exit(-1);
			}
		}
	}

	private static String createTempDir(int port) {
		try {
			File tempDir = File.createTempFile("tomcat.", "." + port);
			tempDir.delete();
			tempDir.mkdir();
			tempDir.deleteOnExit();
			return tempDir.getAbsolutePath();
		} catch (IOException ex) {
			throw new RuntimeException("Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
		}
	}
}
