#!/bin/bash

# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

function helpCommand() {
  echo -e "\nThis script allows the generation and the publication of the \"letscoordinate/frontend\" images\n"
  echo -e "Usage:"
  echo -e "\tbuild_docker_image.sh [OPTIONS]\n"
  echo -e "Options:"
  echo -e "\t--tag\t\t: provide the tag for which the docker images will be generated"
  echo -e "\t\t\t  (this option is required)"
  echo -e "\t--push, -p\t: publish the docker images"
  echo -e "\t\t\t  (when this option is provided, the generated docker images will be pushed to dockerhub)"
  echo -e "\t--help, -h\t: display help\n"
  echo -e "Usage samples:"
  echo -e "\t./build_docker_image.sh --tag=1.3.1.SNAPSHOT"
  echo -e "\t./build_docker_image.sh --tag=1.3.1.RELEASE --push"
  echo -e "\t./build_docker_image.sh --help\n"
}

### PROCESSING STARTS HEAR! ###

tag=NOT_DEFINED
push=false

while [[ $# -gt 0 ]]
do
key="$1"

if [[ ${key} =~ ^'--tag='.* ]]; then
  tag=${key#*'--tag='}
elif [[ ${key} == '--push' || ${key} == '-p' ]]; then
  push=true
elif [[ ${key} == '--help' || ${key} == '-h' ]]; then
  helpCommand
  exit 0
else
  echo 'Unknown param found! (will be ignored) =>' ${key}
fi
shift
done

if [ -z "${tag}" ] || [ "${tag}" = "NOT_DEFINED" ]; then
  echo 'You should provide a valid tag!'
  exit 1;
fi

echo 'TAG:' ${tag}

cd ${LC_HOME}/letsco-front
npm clean-install
ng build --prod --base-href /letsco --deploy-url /letsco/
cd ${LC_HOME}/dockerfiles/front
rm -rf letsco-front
cp -r ${LC_HOME}/letsco-front/dist/letsco-front .
JS_FILES=$(ls letsco-front/ | grep -E '(\.js|\.css|assets)' | xargs echo)
cd letsco-front/ && mv $JS_FILES letsco && cd ${LC_HOME}/dockerfiles/front

echo 'Build letscoordinate/frontend docker images (latest and '${tag}')'
docker build --tag=letscoordinate/frontend:latest -f ./Dockerfile .
docker build --tag=letscoordinate/frontend:${tag} -f ./Dockerfile .

if [ ${push} == true ]; then
  echo 'Push letscoordinate/frontend docker images (latest and '${tag}')'
  docker push letscoordinate/frontend:latest
  docker push letscoordinate/frontend:${tag}
fi