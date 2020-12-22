#!/bin/bash

# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

cd test/prepare-opfab-env/bundles

./send_bundles.sh serviceA cc
./send_bundles.sh serviceA pv
./send_bundles.sh serviceA pvww
./send_bundles.sh serviceA nv
./send_bundles.sh serviceA ps
./send_bundles.sh serviceA pf

./send_bundles.sh serviceB cc
./send_bundles.sh serviceB pv
./send_bundles.sh serviceB pvww
./send_bundles.sh serviceB nv
./send_bundles.sh serviceB ps
./send_bundles.sh serviceB pf

cd ..
java -jar karate.jar prepare-opfab-env.feature
