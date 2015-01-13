#!/bin/bash
set -eux
lein clean
lein midje
lein uberjar
