#!/bin/bash

# Script to set up remote Nagini servers

BASE_PATH=$(cd $(dirname $0)/.. && pwd)
NAGINI_PACKET_PATH=$BASE_PATH

function usage() {
  echo "A script to setup or clean up remote Nagini hosts"
  echo "Format: $0 (install | uninstall) <config-path>"
}

if [[ $# == 2 ]]
then
  NAGINI_SETUP_OPERATION=$1
  NAGINI_CONFIG_PATH=$(cd $2 && pwd)
else
  usage
  exit 1
fi

# if [[ $# == 0 ]]
# then
#     echo "Enter hosts and finish by EOF(^D)"
#     hosts=
#     while read host
#     do
#         hosts="$hosts $host"
#     done
# else
#     usage
#     exit 1
# fi

NAGINI_PROPERTIES_FILE=${NAGINI_CONFIG_PATH}/nagini.properties
NAGINI_HOST_LIST_FILE=${NAGINI_CONFIG_PATH}/host.list

NAGINI_REMOTE_ROOT=$(cat $NAGINI_PROPERTIES_FILE | grep '^server.base.path' | awk -F '=' '{print $2}')
NAGINI_REMOTE_USER=$(cat $NAGINI_PROPERTIES_FILE | grep '^server.user.name' | awk -F '=' '{print $2}')
NAGINI_REMOTE_HOSTS=$(cat ${NAGINI_HOST_LIST_FILE} | awk -F ',' '{print $1}'  | tr '\n' ' ')

echo
echo "Got following remote Nagini hosts:"
for host in ${NAGINI_REMOTE_HOSTS}
do
    echo ${host}
done

echo
echo "Are you sure you want to ${NAGINI_SETUP_OPERATION} Nagini on the hosts (yes/no)? "
PROCEED=
read PROCEED

if [ ${PROCEED} != 'yes' ]; then
  exit 0
fi

echo
echo "Building Nagini client and server ..."
ant dist

echo
echo "Terminating Nagini servers ..."
bash ${NAGINI_PACKET_PATH}/bin/nagini-client.sh control stop -c ${NAGINI_CONFIG_PATH} 

if [ $(uname) != "Darwin" ]; then set -e; fi;

if [ ${NAGINI_SETUP_OPERATION} == 'install' ]; then

  echo
  echo "Copying Nagini packet ..."
  for host in ${NAGINI_REMOTE_HOSTS}
  do
    echo "Copying Nagini to ${host} ..."
    rm -rf ${NAGINI_PACKET_PATH}/rsync.log
    rsync -avzL ${NAGINI_PACKET_PATH} ${NAGINI_CONFIG_PATH} --rsync-path "sudo -u ${NAGINI_REMOTE_USER} rsync" ${host}:${NAGINI_REMOTE_ROOT} >> ${NAGINI_PACKET_PATH}/rsync.log 2>&1
  done

  echo
  echo "Starting Nagini servers ..."
  bash ${NAGINI_PACKET_PATH}/bin/nagini-client.sh control start -c ${NAGINI_CONFIG_PATH}

  sleep 1

  echo
  echo "Ping Nagini servers ..."
  bash ${NAGINI_PACKET_PATH}/bin/nagini-client.sh control ping -c ${NAGINI_CONFIG_PATH}

  exit 0

elif [ ${NAGINI_SETUP_OPERATION} == 'uninstall' ]; then

  echo
  echo "Deleting Nagini packet ..."
  for host in ${NAGINI_REMOTE_HOSTS}
  do
    echo "Deleting Nagini on ${host} ..."
    ssh ${host} "sudo -u ${NAGINI_REMOTE_USER} -sn bash" << EOF
set -ex
rm -rf ${NAGINI_REMOTE_ROOT}/nagini
rm -rf ${NAGINI_REMOTE_ROOT}/config
rm -f ${NAGINI_REMOTE_ROOT}/*.log
EOF
  done

  echo
  exit 0

else

  usage
  exit 1

fi
