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
package org.zephyrsoft.sdb2.remote;

/**
 * ACL v1/connect/+
 * v1/rooms/%s/song
 */
public class RemoteTopic {
	
	final static String HEALTH_DB = "%sdb/v1/health";
	final static int HEALTH_DB_QOS = 1;
	final static boolean HEALTH_DB_RETAINED = true;
	
	final static String SONG = "%srooms/v1/%s/song";
	final static int SONG_QOS = 0;
	final static boolean SONG_RETAINED = true;
	
	final static String POSITION = "%srooms/v1/%s/position";
	final static int POSITION_QOS = 0;
	final static boolean POSITION_RETAINED = true;
	
	final static String PLAYLIST = "%srooms/v1/%s/playlist";
	final static int PLAYLIST_QOS = 0;
	final static boolean PLAYLIST_RETAINED = true;
	
	// Clients publish and subscribe patches (list of songs) to [username/version_id/uuid]:
	final static String PATCHES_LATEST_CHANGES = "%sdb/v1/patches/latest/changes/+/+/+";
	final static int PATCHES_LATEST_CHANGES_QOS = 1;
	final static boolean PATCHES_LATEST_CHANEGS_RETAINED = false;
	public final static int PATCHES_LATEST_CHANGES_ARG_USERNAME = 0;
	public final static int PATCHES_LATEST_CHANGES_ARG_VERSION_ID = 1;
	public final static int PATCHES_LATEST_CHANGES_ARG_UUID = 2;
	
	// Clients subscribe for approving (version_id, timestamp, username, checksum) to: [RETAINED]
	final static String PATCHES_LATEST_VERSION = "%sdb/v1/patches/latest/version";
	final static int PATCHES_LATEST_VERSION_QOS = 1;
	final static boolean PATCHES_LATEST_VERSION_RETAINED = true;
	
	// Clients subscribe for reject (checksum, reason) to:
	final static String PATCHES_LATEST_REJECT = "%sdb/v1/patches/latest/reject";
	final static int PATCHES_LATEST_REJECT_QOS = 1;
	final static boolean PATCHES_LATEST_REJECT_RETAINED = false;
	
	// If client needs a older version, it may request it by publishing to:
	final static String PATCHES_REQUEST_GET = "%sdb/v1/patches/request/%s/get";
	final static int PATCHES_REQUEST_GET_QOS = 1;
	final static boolean PATCHES_REQUEST_GET_RETAINED = false;
	
	// And it will recieve the requested patches to clientid:
	final static String PATCHES_REQUEST_PATCHES = "%sdb/v1/patches/request/%s/patches";
	final static int PATCHES_REQUEST_PATCHES_QOS = 1;
	final static boolean PATCHES_REQUEST_PATCHES_RETAINED = false;
}
