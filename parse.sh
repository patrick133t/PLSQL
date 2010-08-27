#!/bin/sh
BASE=`dirname "$0"`
java -classpath \
    "$BASE/classes:$BASE/lib/antlr-3.2.jar:$BASE/lib/log4j-1.2.15.jar" \
    org.plsql.XSSFinder "$@"
