<?
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

// make the whole (unpacked) distribution available under a PHP-enabled Apache web server
// and point your browser to this file to run the application via Java WebStart

header("Content-Type: application/x-java-jnlp-file");
echo '<?xml version="1.0" encoding="UTF-8"?>' . "\n";
?>
<jnlp spec="1.0+"
	codebase="<? echo "http" . ((!empty($_SERVER['HTTPS'])) ? "s" : "") . "://".$_SERVER['SERVER_NAME'].preg_replace('/webstart.php$/i', '', $_SERVER['REQUEST_URI']); ?>"
	href="webstart.php">
	<information>
		<title>Song Database</title>
		<vendor>ZephyrSoft</vendor>
		<homepage href="http://www.zephyrsoft.net" />
		<description kind="one-line">manage songs texts and present them on a digital projector</description>
		<icon href="img/icon-32.png" kind="default" />
		<shortcut online="false">
			<desktop />
			<menu />
		</shortcut>
	</information>

	<application-desc name="Song Database" main-class="org.zephyrsoft.sdb2.Start" />

	<offline-allowed />
	<update check="background" policy="prompt-update" />

	<security>
		<all-permissions />
	</security>

	<resources>
		<j2se version="1.7+" href="http://java.sun.com/products/autodl/j2se" />
		<jar href="sdb.jar" main="true" />
<?
// backup for above: echo "version=\"" . filemtime(dirname(__FILE__) . DIRECTORY_SEPARATOR . "sdb.jar") . "\"";
$libdir = dirname(__FILE__) . DIRECTORY_SEPARATOR . "lib";
$handle = opendir($libdir);
while (false !== ($file = readdir($handle))) {
	if ($file != "." && $file != ".." && strlen($file)>4 && substr($file, -4)==".jar") {
		echo "		<jar href=\"lib/$file\" />\n";
		// backup for above: version=\"" . filemtime($libdir . DIRECTORY_SEPARATOR . $file) . "\"
	}
}
?>
	</resources>

</jnlp>