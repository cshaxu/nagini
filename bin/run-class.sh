#!/bin/bash

if [ $# -lt 1 ]; then
  echo "Usage: $0 <java-options> <class-name> <class-args>"
  exit 1
fi

JAVA_EXEC=$(which java 2>/dev/null)
BASE_PATH=$(cd $(dirname $0)/.. && pwd)

for file in $BASE_PATH/lib/*.jar $BASE_PATH/dist/*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done

$JAVA_EXEC -cp $CLASSPATH $@
