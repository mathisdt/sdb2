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
 * v1/namespaces/%s/song
 */
public class RemoteTopic {
	
	final static String HEALTH_DB = "%shealth/v1/db";
	final static int HEALTH_DB_QOS = 1;
	final static boolean HEALTH_DB_RETAINED = true;
	
	final static String SONG = "%snamespaces/v1/%s/song";
	final static int SONG_QOS = 0;
	final static boolean SONG_RETAINED = true;
	
	final static String SONG_POSITION = "%snamespaces/v1/%s/songposition";
	final static int SONG_POSITION_QOS = 0;
	final static boolean SONG_POSITION_RETAINED = true;
	
	final static String PLAYLIST = "%snamespaces/v1/%s/playlist";
	final static int PLAYLIST_QOS = 0;
	final static boolean PLAYLIST_RETAINED = true;
	
	// Clients publish and subscribe patches (list of songs) to [username/version_id/uuid]:
	final static String PATCHES_LATEST_PATCH = "%sdb/v1/patches/latest/patch/+/+/+";
	final static int PATCHES_LATEST_PATCH_QOS = 1;
	final static boolean PATCHES_LATEST_PATCH_RETAINED = false;
	public final static int PATCHES_LATEST_PATCH_ARG_USERNAME = 0;
	public final static int PATCHES_LATEST_PATCH_ARG_VERSION_ID = 1;
	public final static int PATCHES_LATEST_PATCH_ARG_UUID = 2;
	
	// Clients subscribe for approving (version_id, timestamp, username, checksum) to: [RETAINED]
	final static String PATCHES_LATEST_VERSION = "%sdb/v1/patches/latest/version";
	final static int PATCHES_LATEST_VERSION_QOS = 1;
	final static boolean PATCHES_LATEST_VERSION_RETAINED = true;
	
	// Clients subscribe for reject (checksum, reason) to:
	final static String PATCHES_LATEST_REJECT = "%sdb/v1/patches/latest/reject";
	final static int PATCHES_LATEST_REJECT_QOS = 1;
	final static boolean PATCHES_LATEST_REJECT_RETAINED = false;
	
	// If client needs a older version, it may request it by publishing the version id to:
	final static String PATCHES_REQUEST_GET = "%sdb/v1/patches/request/%s/get";
	final static int PATCHES_REQUEST_GET_QOS = 1;
	final static boolean PATCHES_REQUEST_GET_RETAINED = false;
	
	// And it will recieve the requested patch to clientid / [username/version_id/uuid]:
	final static String PATCHES_REQUEST_PATCH = "%sdb/v1/patches/request/%s/patch/+";
	final static int PATCHES_REQUEST_PATCH_QOS = 1;
	final static boolean PATCHES_REQUEST_PATCH_RETAINED = false;
	public final static int PATCHES_REQUEST_PATCH_ARG_UUID = 0;
	
	// And it will recieve the requested version (version_id, timestamp, username, checksum) at:
	final static String PATCHES_REQUEST_VERSION = "%sdb/v1/patches/request/%s/version";
	final static int PATCHES_REQUEST_VERSION_QOS = 1;
	final static boolean PATCHES_REQUEST_VERSION_RETAINED = false;
}
