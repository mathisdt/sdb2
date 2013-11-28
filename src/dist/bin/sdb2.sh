#!/bin/sh
BIN_DIR=$(dirname $(readlink -f "$0"))
BASE_DIR=$(readlink -f "$BIN_DIR/..")
JAVA=$(which java)
if [ -z "$JAVA" ]; then
	echo 'Java not found, please download and install the Java Runtime Environment'
	echo 'from here:  =>  http://www.java.com/  <='
else
	$JAVA -Duser.language=de -Duser.country=DE -Dbase.dir="$BASE_DIR" -jar "$BIN_DIR/sdb2.jar" $*
fi
