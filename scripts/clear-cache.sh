#!/bin/bash
set -eux
ssh -t varjocafe sudo rm -v /var/cache/nginx/varjocafe/*
