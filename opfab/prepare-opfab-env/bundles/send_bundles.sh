#!/bin/bash

# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

CLIENT_ID='opfab-client'
CLIENT_SECRET='opfab-keycloak-secret'
KEYCLOAK_TOKEN_ENDPOINT_URL='http://localhost:8080/auth/realms/dev/protocol/openid-connect/token'
USERNAME='admin'
PASSWORD='test'
TOKEN=$( curl -d "client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&grant_type=password" -k ${KEYCLOAK_TOKEN_ENDPOINT_URL} | sed 's/.*access_token":"\(.*\)","expires_in.*/\1/')

if [ $1 = 'serviceA' ] || [ $1 = 'serviceB' ]; then
  if [ $2 = 'pm' ]; then
    cd $1/processmonitoring
  elif [ $2 = 'vfa' ]; then
    cd $1/filea_validation
  elif [ $2 = 'vfb' ]; then
    cd $1/fileb_validation
  elif [ $2 = 'cc' ]; then
    cd $1/cardCreation
  else
    echo "Incorrect arg: $2"
    exit 1
  fi
else
  echo "Incorrect arg: $1"
  exit 1
fi

rm -f bundle.tar.gz
tar -zcvf bundle.tar.gz config.json css template i18n && curl -X POST "http://localhost:2100/businessconfig/processes" -H  "accept: application/json" -H  "Content-Type: multipart/form-data" -F "file=@bundle.tar.gz;type=application/gzip" -H "Authorization: Bearer $TOKEN"
exit 0
