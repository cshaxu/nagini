#!/bin/bash

PATTERN='java.*nagini.NaginiServerCli'

ps -ef | grep $PATTERN

for pid1 in $(ps -ef | grep $PATTERN | awk '{print $2}')
do
  for pid2 in $(ps -ef | grep $PATTERN | awk '{print $2}')
  do
    if [ ${pid1} -eq ${pid2} ];
    then
      sudo -u app kill ${pid1}
    fi
  done
done
