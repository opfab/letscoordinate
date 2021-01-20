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

cp -r ${LC_HOME}/letsco-data-provider/target/letsco-data-provider-${LC_VERSION}*-exec.jar ${LC_HOME}/dockerfiles/data-provider/letsco-data-provider.jar

docker build --tag=letscoordinate/letsco-data-provider:latest -f ./Dockerfile .
docker build --tag=letscoordinate/letsco-data-provider:${tag} -f ./Dockerfile .