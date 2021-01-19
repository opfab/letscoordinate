#!/bin/bash

# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

if [ ! -z $1 ]; then
  tag=$1
  echo "Tag: ${tag}"
else
  echo "You must specify a tag"
  exit 1
fi

cd ../../letsco-front
ng build --prod --base-href /letsco --deploy-url /letsco/
cd -
rm -rf ./letsco-front
cp -r ../../letsco-front/dist/letsco-front .
JS_FILES=$(ls letsco-front/ | grep -E '(\.js|\.css|assets)' | xargs echo)
cd letsco-front/ && mv $JS_FILES letsco && cd -

docker build --tag=letscoordinate/letsco-front:latest -f ./Dockerfile . && \
docker build --tag=letscoordinate/letsco-front:${tag} -f ./Dockerfile . && \
docker stop letsco-front ; docker rm letsco-front ; \
docker run -d -p 4200:8080 --name letsco-front letscoordinate/letsco-front:latest
