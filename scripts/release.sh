#!/bin/bash
set -eux
lein clean
lein midje
lein uberjar
scp scripts/*.sh target/varjocafe-standalone.jar www-prod:varjocafe/
ssh www-prod "cd varjocafe && ./start.sh"
