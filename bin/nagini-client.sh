#!/bin/bash

BASE_PATH=$(cd $(dirname $0)/.. && pwd)
NAGINI_CLIENT_CONFIG_PATH=${BASE_PATH}/config
NAGINI_CLIENT_CONFIG_FILE=${NAGINI_CLIENT_CONFIG_PATH}/nagini.properties

NAGINI_CLIENT_JVM_OPTS=$(cat $NAGINI_CLIENT_CONFIG_FILE | grep '^client.jvm.options' | awk 'gsub(/client.jvm.options=/,""){print $0}')

bash ${BASE_PATH}/bin/run-class.sh \
$NAGINI_CLIENT_JVM_OPTS \
nagini.NaginiClientCli $@ \
--config $NAGINI_CLIENT_CONFIG_PATH
