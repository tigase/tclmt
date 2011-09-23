#!/bin/sh
CLASSPATH="`ls -d libs/*.jar 2>/dev/null | tr '\n' :`${CLASSPATH}"
java -cp $CLASSPATH:jars/tclmt-1.0.0-SNAPSHOT.jar tigase.tclmt.Tclmt $*