#!/bin/sh

set -e

JDK_LINUX=$JAVA_HOME
if [ ! -d "$JDK_LINUX" -o ! -d "$JDK_LINUX/jmods" ]; then
	echo "JAVA_HOME has to point to a directory containing a Linux JDK in version 11"
	exit 1
fi

echo "downloading a Windows JDK from adoptopenjdk.net"
# ATTENTION: has to match the version used on Linux -> .travis.yml
wget -q -O /tmp/windows-jdk.zip 'https://api.adoptopenjdk.net/v2/binary/releases/openjdk11?openjdk_impl=hotspot&os=windows&arch=x64&release=latest&type=jdk&heap_size=normal'
unzip -qq -d /tmp/windows-jdk /tmp/windows-jdk.zip
JDK_WINDOWS=$(ls -d /tmp/windows-jdk/*)
if [ ! -d "$JDK_WINDOWS" -o ! -d "$JDK_WINDOWS/jmods" ]; then
	echo "download of Windows JDK did not work"
	exit 2
fi

DIR=$(dirname $(readlink -f $0))

MODULES=java.desktop,java.scripting,java.xml,java.sql,java.net.http,java.management,java.naming,jdk.crypto.cryptoki,jdk.crypto.ec

echo "creating a Linux JRE"
$JDK_LINUX/bin/jlink \
    --module-path $JDK_LINUX/jmods/ \
    --add-modules $MODULES \
    --output $DIR/target/sdb2-bundle-linux/jre
cp -r $DIR/target/distribution/* $DIR/target/sdb2-bundle-linux/
mkdir -p $DIR/target/sdb2-bundle-linux/bin
cat <<EOF >$DIR/target/sdb2-bundle-linux/bin/sdb2.sh
#!/bin/sh
BIN_DIR=\$(dirname \$(readlink -f "\$0"))
BASE_DIR=\$(readlink -f "\$BIN_DIR/..")
\$BASE_DIR/jre/bin/java -Duser.language=de -Duser.country=DE -Dfile.encoding=UTF-8 -jar "\$BIN_DIR/sdb2.jar" \$*
EOF
chmod a+x $DIR/target/sdb2-bundle-linux/bin/sdb2.sh
cp $DIR/src/main/resources/org/zephyrsoft/sdb2/icon-128.png $DIR/target/sdb2-bundle-linux/bin/icon.png

echo "creating a Windows JRE"
$JDK_LINUX/bin/jlink \
    --module-path $JDK_WINDOWS/jmods/ \
    --add-modules $MODULES \
    --output $DIR/target/sdb2-bundle-windows/jre
cp -r $DIR/target/distribution/* $DIR/target/sdb2-bundle-windows/
mkdir -p $DIR/target/sdb2-bundle-windows/bin
cat <<EOF >$DIR/target/sdb2-bundle-windows/bin/sdb2.bat
..\jre\bin\javaw.exe -Duser.language=de -Duser.country=DE -Dfile.encoding=UTF-8 -jar sdb2.jar $*
EOF
cp $DIR/src/launcher/sdb2.exe $DIR/target/sdb2-bundle-windows/bin/
cp $DIR/src/main/resources/org/zephyrsoft/sdb2/icon.ico $DIR/target/sdb2-bundle-windows/bin/icon.ico

echo "packing the distributions"
cd $DIR/target
zip -qr $DIR/target/sdb2-bundle-linux.zip sdb2-bundle-linux
zip -qr $DIR/target/sdb2-bundle-windows.zip sdb2-bundle-windows
