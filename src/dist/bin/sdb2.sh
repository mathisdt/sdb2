#!/bin/sh
BIN_DIR=$(dirname $(readlink -f "$0"))
BASE_DIR=$(readlink -f "$BIN_DIR/..")
JAVA=$(which java)
if [ -z "$JAVA" ]; then
	echo 'Java not found - please download and install OpenJDK 11 or higher,'
	echo 'for example from here: https://adoptopenjdk.net'
else
	$JAVA -Duser.language=de -Duser.country=DE -Dfile.encoding=UTF-8 -Dbase.dir="$BASE_DIR" -jar "$BIN_DIR/sdb2.jar" $*
fi
