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
package org.zephyrsoft.util;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.util.jenkins.Artifact;
import org.zephyrsoft.util.jenkins.FreeStyleBuild;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.thoughtworks.xstream.XStream;

/**
 * Extracts the current version from the manifest and checks for updates.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class VersionTools {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersionTools.class);
	
	private static final String JENKINS_INFO_URL = "https://dev.zephyrsoft.org/jenkins/job/SDB2/lastSuccessfulBuild/api/xml";
	private static final String JENKINS_DOWNLOAD_URL = "https://dev.zephyrsoft.org/jenkins/job/SDB2/lastSuccessfulBuild/artifact/target/";
	
	public static String getCurrent() {
		String version = getImplementationVersion();
		String timestamp = JarTools.getAttributeFromManifest(VersionTools.class, "Build-Timestamp");
		if (version != null && timestamp != null) {
			version += " (" + timestamp + ")";
		} else {
			version = "development snapshot";
		}
		return version;
	}
	
	/**
	 * @return the latest version update or {@code null} if the running version is the latest one
	 */
	public static VersionUpdate getLatest() {
		FreeStyleBuild jenkinsInfo = getJenkinsInfo();
		String versionFromJenkins = null;
		if (jenkinsInfo != null
			&& jenkinsInfo.getArtifacts() != null
			&& !jenkinsInfo.getArtifacts().isEmpty()
			&& jenkinsInfo.getArtifacts().get(0) != null) {
			versionFromJenkins = extractVersionFromArtifactFilename(jenkinsInfo.getArtifacts().get(0).getFileName());
		}
		if (versionFromJenkins == null || !versionFromJenkins.equals(getCurrent())) {
			return null;
		} else {
			return new VersionUpdate(versionFromJenkins, JENKINS_DOWNLOAD_URL);
		}
	}
	
	private static String extractVersionFromArtifactFilename(String filename) {
		if (filename != null) {
			return filename.replaceAll("^sdb2-", "").replaceAll("\\.zip$", "");
		} else {
			return null;
		}
	}
	
	private static FreeStyleBuild getJenkinsInfo() {
		try {
			URL url = new URL(JENKINS_INFO_URL);
			String rawData = Resources.toString(url, Charsets.UTF_8);
			XStream xstream = initXStream();
			FreeStyleBuild jenkinsInfo = (FreeStyleBuild) xstream.fromXML(rawData);
			return jenkinsInfo;
		} catch (IOException e) {
			LOG.warn("could not get update information from {}", JENKINS_INFO_URL, e);
			return null;
		}
	}
	
	private static XStream initXStream() {
		XStream xstream = new XStream();
		
		// aliases and omitted fields of model classes are defined via annotations
		xstream.processAnnotations(FreeStyleBuild.class);
		xstream.processAnnotations(Artifact.class);
		
		// unknown XML elements can be ignored
		xstream.ignoreUnknownElements();
		
		return xstream;
	}
	
	private static String getImplementationVersion() {
		return JarTools.getAttributeFromManifest(VersionTools.class, "Implementation-Version");
	}
	
	public static class VersionUpdate {
		private String versionNumber;
		private String updateUrl;
		
		public VersionUpdate(String versionNumber, String updateUrl) {
			this.versionNumber = versionNumber;
			this.updateUrl = updateUrl;
		}
		
		public String getVersionNumber() {
			return versionNumber;
		}
		
		public String getUpdateUrl() {
			return updateUrl;
		}
	}
	
}
