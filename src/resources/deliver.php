<?php
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

if (file_exists('constants.php')) {
	include('constants.php');
} else {
	die("constants not found");
}

$versionId = $_REQUEST['version-id'];
if ($versionId==BUILD_TIMESTAMP) {

	$file = $_SERVER['PATH_INFO'];

	// sanitize file name
	$file = preg_replace('/[^-_\.a-zA-Z0-9]/', '', $file);

	if ($file=='sdb.jar') {
		$path = dirname(__FILE__) . '/' . $file;
	} else {
		$path = dirname(__FILE__) . '/lib/' . $file;
	}
	if (file_exists($path) && is_readable($path) ) {
		header('Content-Type: application/java-archive');
		header('x-java-jnlp-version-id: ' . BUILD_TIMESTAMP);
		header('Last-Modified: ' . BUILD_TIMESTAMP_READABLE);
		header("Content-length: ".filesize($path));
		readfile($path);
	} else {
		header("HTTP/1.0 404 Not Found");
		die('JAR file not available.');
	}
} else {
	header("HTTP/1.0 404 Not Found");
	die('JAR file with version-id "' . $versionId . '" not available. Try requesting version-id "' . BUILD_TIMESTAMP . '".');
}

?>