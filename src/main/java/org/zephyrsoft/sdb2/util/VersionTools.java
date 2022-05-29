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
package org.zephyrsoft.sdb2.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Extracts the current version from the manifest and checks for updates.
 */
public class VersionTools {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersionTools.class);
	
	private VersionTools() {
		// only static use
	}
	
	public static String getCurrent() {
		String version = getImplementationVersion();
		LocalDateTime timestamp = getTimestampAsLocalDateTime();
		if (version != null && timestamp != null) {
			version += " (" + DateTools.formatDateTime(timestamp) + ")";
		} else {
			version = "development snapshot";
		}
		return version;
	}
	
	public static String getGitCommitHash() {
		return JarTools.getAttributeFromManifest(VersionTools.class, "Git-Commit-ID");
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
			HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/mathisdt/sdb2/releases/latest"))
				.GET()
				.build();
			HttpResponse<String> latestReleaseResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			
			if (latestReleaseResponse == null) {
				return null;
			}
			
			JsonObject latestRelease = JsonParser.parseString(latestReleaseResponse.body()).getAsJsonObject();
			
			LocalDateTime latestReleaseTimestamp = DateTools.parseDateTime(latestRelease.get("published_at").getAsString());
			LocalDateTime ownTimestamp = getTimestampAsLocalDateTime();
			
			LOG.info("own timestamp: {} / latest release timestamp: {}", ownTimestamp, latestReleaseTimestamp);
			if (ownTimestamp == null || latestReleaseTimestamp == null
				|| DateTools.isMax15MinutesLater(latestReleaseTimestamp, ownTimestamp)) {
				// is already latest release or we cannot tell
				return null;
			} else {
				return new VersionUpdate(DateTools.formatDateTime(latestReleaseTimestamp),
					latestRelease.get("html_url").getAsString());
			}
		} catch (Exception e) {
			LOG.warn("error communicating with GitHub", e);
			return null;
		}
	}
	
	public static class VersionUpdate {
		private final String versionTimestamp;
		private final String webUrl;
		
		public VersionUpdate(String versionTimestamp, String webUrl) {
			this.versionTimestamp = versionTimestamp;
			this.webUrl = webUrl;
		}
		
		public String getVersionTimestamp() {
			return versionTimestamp;
		}
		
		public String getWebUrl() {
			return webUrl;
		}
	}
	
}
