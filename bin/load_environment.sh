#!/bin/bash

# Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
# Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.


######################################################
#           START CUSTOMIZABLE VARIABLES             #
######################################################
export SERVER_IP=localhost
######################################################
#            END CUSTOMIZABLE VARIABLES              #
######################################################

echo USER_ID="$(id -u)" > .env
echo USER_GID="$(id -g)" >> .env

docker network create --driver bridge opfab-network
export DOCKER_IP=$(docker inspect opfab-network --format '{{range .IPAM.Config}}{{.Gateway}}{{end}}' "$@")

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
export LC_HOME=$(realpath $DIR/..)
export LC_VERSION=$(cat "$LC_HOME/LETSCO_SHORT_VERSION")
export OF_VERSION=$(cat "$LC_HOME/OPFAB_FULL_VERSION")

echo -e "\033[0;32mPREPARING ENVIRONMENT VARIABLES...\033[0m"
echo
echo "SERVER_IP  =" $SERVER_IP
echo "DOCKER_IP  =" $DOCKER_IP
echo "LC_HOME    =" $LC_HOME
echo "LC_VERSION =" $LC_VERSION
echo "OF_VERSION =" $OF_VERSION
echo

echo -e "\033[0;32mINSTALLING REQUIRED TOOLS...\033[0m"

sdk install java 11.0.10-zulu
sdk use java 11.0.10-zulu

sdk install maven 3.5.3
sdk use maven 3.5.3

nvm install v10.16.3
nvm use v10.16.3

echo -e "\033[0;32mGENERATION CONFIG FILES FROM TEMPLATES...\033[0m"

sed "s/\${SERVER_IP}/$SERVER_IP/g" ${LC_HOME}/bin/config/templates/env.js.template > ${LC_HOME}/letsco-front/src/letsco-env/env.js
sed "s/\${SERVER_IP}/$SERVER_IP/g" ${LC_HOME}/bin/config/templates/ui-menu.json.template > ${LC_HOME}/opfab/operatorfabric-getting-started/server/ui-config/ui-menu.json
sed "s/\${SERVER_IP}/$SERVER_IP/g" ${LC_HOME}/bin/config/templates/web-ui.json.template > ${LC_HOME}/opfab/operatorfabric-getting-started/server/ui-config/web-ui.json
sed "s/\${SERVER_IP}/$SERVER_IP/g" ${LC_HOME}/bin/config/templates/karate-config.js.template > ${LC_HOME}/opfab/prepare-opfab-env/karate-config.js
sed "s/\${DOCKER_IP}/$DOCKER_IP/g" ${LC_HOME}/bin/config/templates/nginx.conf.template > ${LC_HOME}/opfab/operatorfabric-getting-started/server/nginx.conf
sed "s/\${DOCKER_IP}/$DOCKER_IP/g" ${LC_HOME}/bin/config/templates/cards-publication.yml.template > ${LC_HOME}/opfab/operatorfabric-getting-started/server/docker-configurations/cards-publication.yml
sed "s/\${DOCKER_IP}/$DOCKER_IP/g" ${LC_HOME}/bin/config/templates/common.yml.template > ${LC_HOME}/opfab/operatorfabric-getting-started/server/docker-configurations/common.yml
