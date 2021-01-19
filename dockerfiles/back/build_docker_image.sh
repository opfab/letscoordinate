#!/bin/bash

# Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
# Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
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

cd ../../letsco-api
mvn clean package -DskipTests=true
cd -
cp -r ../../letsco-api/target/letsco-api-${LC_VERSION}*-exec.jar ./letsco-api.jar

docker build --tag=letscoordinate/letsco-api:latest -f ./Dockerfile .
docker build --tag=letscoordinate/letsco-api:${tag} -f ./Dockerfile .