#!/bin/bash

# Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
# Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.


# build docker images for letsco-api
cd ${LC_HOME}/dockerfiles/back
source ./build_docker_image.sh ${LC_VERSION}.snapshot

# build docker images for letsco-data-provider
cd ${LC_HOME}/dockerfiles/data-provider
source ./build_docker_image.sh ${LC_VERSION}.snapshot

# build docker images for letsco-front
cd ${LC_HOME}/dockerfiles/front
source ./build_docker_image.sh ${LC_VERSION}.snapshot