#!/bin/bash

# Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
# Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

function helpCommand() {
	echo -e "\nThis script allows the command of the Let's Coordinate server\n"
	echo -e "Usage:"
	echo -e "\tserver.sh [OPTIONS] [COMMAND]\n"
	echo -e "Options:"
  echo -e "\t-b, --build\t: build letsco docker images locally before starting server"
  echo -e "\t\t\t  (useful with start and restart commands)"
  echo -e "\t-i, --init\t: initialize opfab database with required data (users, groups, perimeters, ...) and send bundles and config files"
  echo -e "\t\t\t  (useful with start and restart commands)"
  echo -e "\t-f, --first-init: equivalent to using --build and --init options together"
  echo -e "\t\t\t  (useful with start and restart commands)"
  echo -e "\t-h, --help\t: display help\n"
	echo -e "Commands:"
  echo -e "\tstart\t\t: start the server"
  echo -e "\trestart\t\t: stop and start the server"
  echo -e "\tstop\t\t: stop the server"
  echo -e "\tdown\t\t: stop the server and remove docker containers, networks, images, and volumes"
  echo -e "\tstatus\t\t: display status of services\n"
}

function startCommand() {
  echo -e "\033[0;32mSTARTING OPERATORFABRIC SERVER...\033[0m"
  cd ${LC_HOME}/opfab/operatorfabric-getting-started/server
  source startServer.sh
  echo -e "\033[0;32mWAITING 5s...\033[0m"
  sleep 5s
  echo -e "\033[0;32mSTARTING LET'S COORDINATE SERVER...\033[0m"
  cd ${LC_HOME}/bin
  docker-compose up -d
}

function stopCommand() {
  echo -e "\033[0;32mSTOPPING LET'S COORDINATE SERVER...\033[0m"
  cd ${LC_HOME}/bin
  docker-compose stop
  echo -e "\033[0;32mSTOPPING OPERATORFABRIC SERVER...\033[0m"
  cd ${LC_HOME}/opfab/operatorfabric-getting-started/server
  docker-compose stop
}

function downCommand() {
  echo -e "\033[0;32mSTOPPING AND REMOVING LET'S COORDINATE SERVER...\033[0m"
  cd ${LC_HOME}/bin
  docker-compose down
  echo -e "\033[0;32mSTOPPING AND REMOVING OPERATORFABRIC SERVER...\033[0m"
  cd ${LC_HOME}/opfab/operatorfabric-getting-started/server
  docker-compose down
}

function statusCommand() {
  cd ${LC_HOME}/opfab/operatorfabric-getting-started/server
  docker-compose ps
  cd ${LC_HOME}/bin
  docker-compose ps
}

function commandNotImplemented() {
  echo -e "\033[0;31m  This function is not yet implemented! \033[0m"
}

function buildSnapshotDockerImagesIfAsked() {
  if [ ${build} = true ] || [ ${firstInit} = true ]; then
      echo -e "\033[0;32mBUILDING LETSCO SNAPSHOT DOCKER IMAGES...\033[0m"
      echo
      ./build_snapshot_docker_images.sh
      echo
  fi
}

function initIfAsked() {
  if [ ${init} = true ] || [ ${firstInit} = true ]; then
      echo -e "\033[0;32mINITIALIZING OPERATORFABRIC DATABASE AND SENDING BUNDLES AND CONFIGS...\033[0m"
      echo
      cd ${LC_HOME}
      ./opfab/prepare-opfab-env/prepare-opfab-env.sh
      sudo chown -R $USER:$USER ${LC_HOME}/opfab/operatorfabric-getting-started/server/businessconfig-storage
      cd ${LC_HOME}/opfab/prepare-opfab-env/web-ui-config
      ./send-processes-groups.sh
      cd ${LC_HOME}/opfab/prepare-opfab-env/monitoringConfig
      ./loadMonitoringConfig.sh monitoringConfig.json
      echo
  fi
}

### PROCESSING STARTS HEAR! ###

# Check if environment variables loaded
if [ "${LC_HOME}" = "" ] || [ "${LC_VERSION}" = "" ] || [ "${OF_VERSION}" = "" ]; then
    echo -e "\033[0;31m\nOups! It seems that you forgot to load environment variables! Please run the following command and try again:\033[0m"
    echo -e "\033[0;31m\n\tsource ./load_environment.sh\n \033[0m"
    exit 1;
fi

build=false
init=false
firstInit=false

while [[ $# -gt 0 ]]
do
key="$1"
case ${key} in
    -b|--build)
    build=true
    shift # go to next argument
    ;;
    -i|--init)
    init=true
    shift # go to next argument
    ;;
    -f|--first-init)
    firstInit=true
    shift # go to next argument
    ;;
    -h|--help)
    helpCommand
    exit 0
    ;;
    start|restart|stop|down|status|help)
    if [ ${command} ]; then
        echo -e "\nMultiple commands detected, you should set only one command! => [ ${command} ; ${key}]\n"
        exit 1
    fi
    command=${key}
    shift # go to next argument
    ;;
    *) # unknown option or command
    echo -e "\nUnknown option or command found! => ${key}\n"
    exit 1
    ;;
esac
done

if [ "${command}" = "" ]; then
    echo -e "\nNo command found! to see available commands please try:\n\n\tserver.sh --help\n"
    exit 1;
fi

currentPath=$(pwd)

case ${command} in
  start)
  buildSnapshotDockerImagesIfAsked
  startCommand
  initIfAsked
  ;;
  restart)
  stopCommand
  echo -e "\033[0;32mWAITING 5s...\033[0m"
  sleep 5s
  buildSnapshotDockerImagesIfAsked
  startCommand
  initIfAsked
  ;;
  stop)
  stopCommand
  ;;
  down)
  downCommand
  ;;
  status)
  statusCommand
  ;;
  *) # unknown option
  helpCommand
  ;;
esac

cd $currentPath

