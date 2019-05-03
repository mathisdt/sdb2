#!/bin/sh

set -e

JDK_LINUX=$JAVA_HOME
if [ ! -d "$JDK_LINUX" -o ! -d "$JDK_LINUX/jmods" ]; then
	echo "JAVA_HOME has to point to a directory containing a modularized Linux JDK (version 11 or higher)"
	exit 1
fi

wget -O /tmp/windows-jdk.zip 'https://api.adoptopenjdk.net/v2/binary/releases/openjdk11?openjdk_impl=hotspot&os=windows&arch=x64&release=latest&type=jdk&heap_size=normal'
unzip -d /tmp/windows-jdk /tmp/windows-jdk.zip
JDK_WINDOWS=$(ls -d /tmp/windows-jdk/*)
if [ ! -d "$JDK_WINDOWS" -o ! -d "$JDK_WINDOWS/jmods" ]; then
	echo "download of Windows JDK did not work"
	exit 2
fi

DIR=$(dirname $(readlink -f $0))

MODULES=java.desktop,java.scripting,java.xml,java.sql,java.management,java.naming,jdk.crypto.cryptoki,jdk.crypto.ec

$JDK_LINUX/bin/jlink \
    --module-path $JDK_LINUX/jmods/ \
    --add-modules $MODULES \
    --output $DIR/target/distribution-linux/jre
$JDK_LINUX/bin/jlink \
    --module-path $JDK_WINDOWS/jmods/ \
    --add-modules $MODULES \
    --output $DIR/target/distribution-windows/jre

cp -r $DIR/target/distribution/* $DIR/target/distribution-linux/
cp -r $DIR/target/distribution/* $DIR/target/distribution-windows/

cat <<EOF >$DIR/target/distribution-linux/bin/sdb2.sh
#!/bin/sh
BIN_DIR=\$(dirname \$(readlink -f "\$0"))
BASE_DIR=\$(readlink -f "\$BIN_DIR/..")
\$BASE_DIR/jre/bin/java -Duser.language=de -Duser.country=DE -Dfile.encoding=UTF-8 -jar "\$BIN_DIR/sdb2.jar" \$*
EOF
chmod a+x $DIR/target/distribution-linux/bin/sdb2.sh

cat <<EOF >$DIR/target/distribution-windows/bin/sdb2.bat
..\jre\bin\javaw.exe -Duser.language=de -Duser.country=DE -Dfile.encoding=UTF-8 sdb2.jar $*
EOF
rm $DIR/target/distribution-windows/bin/sdb2.sh

zip -qr $DIR/target/sdb2-bundle-for-linux.zip $DIR/target/distribution-linux/*
zip -qr $DIR/target/sdb2-bundle-for-windows.zip $DIR/target/distribution-windows/*
