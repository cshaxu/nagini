#!/bin/bash

if [[ $# != 2 ]]
then
  echo "Usage: $0 <config-path> <host-name>"
  exit 1
fi

BASE_PATH=$(cd $(dirname $0)/.. && pwd)

NAGINI_SERVER_CONFIG_PATH=$(cd $1 && pwd)
NAGINI_SERVER_HOST_NAME=$2

NAGINI_SERVER_CONFIG_PROP_FILE=${NAGINI_SERVER_CONFIG_PATH}/nagini.properties
NAGINI_SERVER_JVM_OPTS=$(cat $NAGINI_SERVER_CONFIG_PROP_FILE | grep '^server.jvm.options' | awk 'gsub(/server.jvm.options=/,""){print $0}')
NAGINI_SERVER_BASE_PATH=$(cat $NAGINI_SERVER_CONFIG_PROP_FILE | grep '^server.base.path' | awk -F '=' '{print $2}')
NAGINI_SERVER_LOG_FILE=${NAGINI_SERVER_BASE_PATH}/$(cat $NAGINI_SERVER_CONFIG_PROP_FILE | grep '^server.logfile.name' | awk -F '=' '{print $2}')

echo $(date) > $NAGINI_SERVER_LOG_FILE
nohup bash ${BASE_PATH}/bin/run-class.sh $NAGINI_SERVER_JVM_OPTS nagini.NaginiServerCli $NAGINI_SERVER_CONFIG_PATH $NAGINI_SERVER_HOST_NAME >> $NAGINI_SERVER_LOG_FILE 2>&1 &
