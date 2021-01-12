# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

Feature: Get Token

  Scenario:
    Given url opfabUrl + 'auth/token'
    And form field username = username
    And form field password = 'test'
    And form field grant_type = 'password'
    And form field client_id = 'opfab-client'
    When method post
    Then status 200
    And def authToken = response.access_token