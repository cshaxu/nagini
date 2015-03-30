#!/bin/bash

PATTERN='java.*nagini.NaginiServerCli'

BASE_PATH=$(cd $(dirname $0)/.. && pwd)
NAGINI_LOCAL_ROOT=$BASE_PATH
NAGINI_LOCAL_CONFIG_PATH=${NAGINI_LOCAL_ROOT}/config
NAGINI_LOCAL_CONFIG_FILE=${NAGINI_LOCAL_CONFIG_PATH}/nagini.properties
USERNAME=$(cat $NAGINI_LOCAL_CONFIG_FILE | grep '^server.user.name' | awk -F '=' '{print $2}')

NAGINI_CANDIDATE_PID_LIST_1=$(ps -ef | grep ${PATTERN} | awk '{print $2}')
NAGINI_CANDIDATE_PID_LIST_2=$(ps -ef | grep ${PATTERN} | awk '{print $2}')

NAGINI_PID_LIST=""
for pid1 in ${NAGINI_CANDIDATE_PID_LIST_1}
do
  for pid2 in ${NAGINI_CANDIDATE_PID_LIST_2} 
  do
    if [ ${pid1} -eq ${pid2} ];
    then
      NAGINI_PID_LIST="${NAGINI_PID_LIST} ${pid1}"
    fi
  done
done

COUNT=0
for PID in ${NAGINI_PID_LIST}
do
  COUNT=$((${COUNT}+1))
  for PINFO in $(ps -ef | grep ${PATTERN} | tr ' ' '@')
  do
    PINFO=$(echo ${PINFO} | tr '@' ' ')
    PINFO_ID=$(echo ${PINFO} | awk '{print $2}')
    if [ ${PINFO_ID} -eq ${PID} ];
    then
      echo ${PINFO}
    fi 
  done
done

PROCEED='no'
if [ ${COUNT} -ge 1 ];
then
  echo "Are you sure you want to kill Nagini server processes (yes/no)? "
  read PROCEED
fi

if [ ${PROCEED} != 'yes' ]; then
  exit 0
fi

for PID in ${NAGINI_PID_LIST}
do
  sudo -u ${USERNAME} kill ${PID}
done
