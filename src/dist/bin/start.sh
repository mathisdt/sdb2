#!/bin/sh
BIN_DIR=$(dirname $(readlink -f "$0"))
BASE_DIR=$(readlink -f "$BIN_DIR/..")
java -Duser.language=de -Duser.country=DE -Dbase.dir="$BASE_DIR" -jar "$BIN_DIR/sdb2.jar" $*
