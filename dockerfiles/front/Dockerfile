# Copyright (c) 2020, RTE (https://www.rte-france.com)
# Copyright (c) 2020 RTE international (https://www.rte-international.com)
# See AUTHORS.txt
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# SPDX-License-Identifier: MPL-2.0
# This file is part of the Let’s Coordinate project.

FROM nginx

RUN rm /etc/nginx/nginx.conf
RUN mkdir /var/www
RUN mkdir /var/www/letsco

COPY ./nginx.conf /etc/nginx
COPY ./letsco-front /var/www/letsco

RUN rm /var/www/letsco/letsco-env/env.js

EXPOSE 80
EXPOSE 8080
