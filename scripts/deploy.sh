#!/bin/bash
set -eux
ansible-playbook provisioning/webserver.yml -i provisioning/production_hosts -vv
