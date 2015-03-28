#!/bin/bash

PATTERN='java.*nagini.NaginiServerCli'

ps -ef | grep $PATTERN
