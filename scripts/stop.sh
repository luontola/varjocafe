#!/bin/bash
set -eu
pkill -f 'java .*varjocafe'
sleep 1
echo "Stopped"
