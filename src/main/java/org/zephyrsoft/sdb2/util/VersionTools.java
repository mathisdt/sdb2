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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the current version from the manifest and checks for updates.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class VersionTools {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersionTools.class);
	
	public static String getCurrent() {
		String version = getImplementationVersion();
		LocalDateTime timestamp = getTimestampAsLocalDateTime();
		if (version != null && timestamp != null) {
			version += " (" + DateTools.format(timestamp) + ")";
		} else {
			version = "development snapshot";
		}
		return version;
	}
	
	private static String getImplementationVersion() {
		return JarTools.getAttributeFromManifest(VersionTools.class, "Implementation-Version");
	}
	
	private static String getTimestamp() {
		return JarTools.getAttributeFromManifest(VersionTools.class, "Build-Timestamp");
	}
	
	private static LocalDateTime getTimestampAsLocalDateTime() {
		String timestamp = getTimestamp();
		return DateTools.parseDateTime(timestamp);
	}
	
	/**
	 * @return the latest version update or {@code null} if the running version is the latest one
	 */
	public static VersionUpdate getLatest() {
		try {
			GitHub gitHub = GitHub.connectAnonymously();
			List<GHRelease> releases = gitHub.getUser("mathisdt").getRepository("sdb2").listReleases().asList();
			GHRelease latestRelease = releases == null || releases.isEmpty()
				? null
				: releases.get(0);
			if (latestRelease == null) {
				return null;
			}
			LocalDateTime latestReleaseTimestamp = DateTools.toLocalDateTime(latestRelease.getPublished_at());
			LocalDateTime ownTimestamp = getTimestampAsLocalDateTime();
			
			LOG.info("own timestamp: {} / latest release timestamp: {}", ownTimestamp, latestReleaseTimestamp);
			if (ownTimestamp == null || latestReleaseTimestamp == null
				|| DateTools.isMax15MinutesLater(latestReleaseTimestamp, ownTimestamp)) {
				// is already latest release or we cannot tell
				return null;
			} else {
				if (latestRelease.getAssets() == null || latestRelease.getAssets().isEmpty()) {
					LOG.error("release {} does not have any assets", latestRelease.getName());
					return null;
				} else {
					return new VersionUpdate(DateTools.format(latestReleaseTimestamp),
						latestRelease.getHtmlUrl().toString(),
						latestRelease.getAssets().get(0).getBrowserDownloadUrl());
				}
			}
		} catch (IOException ioe) {
			LOG.error("error communicating with GitHub", ioe);
			return null;
		}
	}
	
	public static class VersionUpdate {
		private final String versionTimestamp;
		private final String webUrl;
		private final String updateUrl;
		
		public VersionUpdate(String versionTimestamp, String webUrl, String updateUrl) {
			this.versionTimestamp = versionTimestamp;
			this.webUrl = webUrl;
			this.updateUrl = updateUrl;
		}
		
		public String getVersionTimestamp() {
			return versionTimestamp;
		}
		
		public String getWebUrl() {
			return webUrl;
		}
		
		public String getUpdateUrl() {
			return updateUrl;
		}
	}
	
}
