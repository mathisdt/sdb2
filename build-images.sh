#!/bin/sh

set -e
set -x

JDK=~/software/jdk12
DIR=$(dirname $(readlink -f $0))

ls $DIR/target/distribution/lib/*.jar | while read JAR; do
	echo "trying to squash jar $JAR (if it is a multi-release jar) ..."
	TMPDIR=$(mktemp -d)
	cd $TMPDIR

	unzip -q $JAR
	if [ -d META-INF/versions/12 ]; then
		cp -r META-INF/versions/12/* .
		rm -r META-INF/versions
		rm $JAR
		zip -rq $JAR .
		echo "  done"
	elif [ -d META-INF/versions/11 ]; then
		cp -r META-INF/versions/11/* .
		rm -r META-INF/versions
		rm $JAR
		zip -rq $JAR .
		echo "  done"
	elif [ -d META-INF/versions/10 ]; then
		cp -r META-INF/versions/10/* .
		rm -r META-INF/versions
		rm $JAR
		zip -rq $JAR .
		echo "  done"
	elif [ -d META-INF/versions/9 ]; then
		cp -r META-INF/versions/9/* .
		rm -r META-INF/versions
		rm $JAR
		zip -rq $JAR .
		echo "  done"
	else
		echo "  not a multi-release jar"
	fi

	cd $DIR
	rm -r $TMPDIR
done

ls $DIR/target/distribution/lib/*.jar | while read JAR; do
	echo "working on $JAR ..."
	SEARCH_RESULT=$(unzip -l $JAR | grep module-info.class || echo "")
	if [ -z $SEARCH_RESULT ]; then
		echo "module-info.class not found, creating it"
		TMPDIR=$(mktemp -d)
		cd $TMPDIR

		LIBS_WITHOUT_CURRENT=$(ls $DIR/target/distribution/lib/*.jar | grep -v "$JAR" | xargs -I{} echo -n "{}:")
		$JDK/bin/jdeps -v --multi-release base --generate-module-info . --module-path ${LIBS_WITHOUT_CURRENT}$JDK/jmods/ $JAR
		MODULE_NAME=$(find . -type f -name module-info.java | sed -e 's#^\./##' -e 's#/.*$##')
		echo "using name $MODULE_NAME"
		cd $MODULE_NAME
		unzip $JAR
		$JDK/bin/javac -p $MODULE_NAME:$DIR/target/distribution/lib/:$DIR/target/distribution/bin/sdb2.jar:$JDK/jmods/ -d . module-info.java
		zip -r $JAR module-info.class

		cd $DIR
		rm -r $TMPDIR
	else
		echo "module-info.class found"
	fi
done

$JDK/bin/jlink \
    --module-path $DIR/target/distribution/lib/:$DIR/target/distribution/bin/sdb2.jar:$JDK/jmods/ \
    --add-modules org.zephyrsoft.sdb2 \
    --launcher sdb2=org.zephyrsoft.sdb2/org.zephyrsoft.sdb2.Start \
    --output $DIR/target/images
