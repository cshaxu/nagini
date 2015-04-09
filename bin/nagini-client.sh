#!/bin/bash

BASE_PATH=$(cd $(dirname $0)/.. && pwd)

OPTION=
NAGINI_CLIENT_CONFIG_PATH=
for ARGUMENT in "$@";
do
  if [ "${OPTION}" == "-c" ] || [ "${OPTION}" == "--config" ]; 
  then
    NAGINI_CLIENT_CONFIG_PATH=$(cd ${ARGUMENT} && pwd)
    break
  else
    OPTION=${ARGUMENT}
  fi
done

NAGINI_CLIENT_CONFIG_FILE=${NAGINI_CLIENT_CONFIG_PATH}/nagini.properties

if [ -e ${NAGINI_CLIENT_CONFIG_FILE} ];
then
  NAGINI_CLIENT_JVM_OPTS=$(cat $NAGINI_CLIENT_CONFIG_FILE | grep '^client.jvm.options' | awk 'gsub(/client.jvm.options=/,""){print $0}')
else
  NAGINI_CLIENT_JVM_OPTS=
fi

bash ${BASE_PATH}/bin/run-class.sh \
$NAGINI_CLIENT_JVM_OPTS \
nagini.NaginiClientCli $@
