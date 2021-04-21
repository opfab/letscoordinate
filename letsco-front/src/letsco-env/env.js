/*
 * Copyright (c) 2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

(function (window) {
    window.__env = window.__env || {};

    window.__env.serverUrl = 'http://localhost/letsco/api';
    window.__env.openidUrl = 'http://localhost';
    window.__env.openidRealmsUrlPrefix = '/auth/realms/';
    window.__env.openidRealms = 'dev';
    window.__env.openidRealmsUrlSuffix = '/protocol/openid-connect/';
    window.__env.openidAuthEndpoint = 'auth';
    window.__env.openidTokenEndpoint = 'token';
    window.__env.openidClientId = 'opfab-client';
    window.__env.openidBasicAuth = 'b3BmYWItY2xpZW50Om9wZmFiLWtleWNsb2FrLXNlY3JldA==';

    // Whether or not to enable debug mode
    // Setting this to false will disable console output
    window.__env.enableDebug = true;

  }(this));
