#!/bin/sh
set -eu
ssh varjocafe "grep 'end.*uri: /$' /opt/varjocafe/log/varjocafe.log | tail -n 100"
