#!/bin/bash
set -eu
./stop.sh
export TZ='Europe/Helsinki'
nohup java -jar varjocafe-standalone.jar >> varjocafe.out 2>> varjocafe.err < /dev/null &
