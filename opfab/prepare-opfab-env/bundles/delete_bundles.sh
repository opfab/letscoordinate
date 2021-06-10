#!/bin/bash

# Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
# Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

CLIENT_ID='opfab-client'
CLIENT_SECRET='opfab-keycloak-secret'
KEYCLOAK_TOKEN_ENDPOINT_URL='http://localhost:89/auth/realms/dev/protocol/openid-connect/token'
USERNAME='admin'
PASSWORD='test'
TOKEN=$( curl -d "client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&grant_type=password" -k ${KEYCLOAK_TOKEN_ENDPOINT_URL} | sed 's/.*access_token":"\(.*\)","expires_in.*/\1/')

RESULT=$( curl --write-out %{http_code} --silent --output /dev/null  -X DELETE "http://localhost:2100/businessconfig/processes" -H  "accept: application/json" -H  "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" )
echo "Response status: $RESULT"
exit 0
