#!/bin/bash

# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

cd ${LC_HOME}/opfab/prepare-opfab-env/bundles

./send_bundles.sh serviceA cc
./send_bundles.sh serviceA pm
./send_bundles.sh serviceA vfa
./send_bundles.sh serviceA vfb
./send_bundles.sh serviceA co
./send_bundles.sh serviceA cof

./send_bundles.sh serviceB cc
./send_bundles.sh serviceB pm
./send_bundles.sh serviceB vfa
./send_bundles.sh serviceB vfb
./send_bundles.sh serviceB co
./send_bundles.sh serviceB cof

cd ..
java -jar karate.jar prepare-opfab-env.feature
