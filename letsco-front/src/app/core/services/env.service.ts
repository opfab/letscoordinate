/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {

  // The values that are defined here are the default values that can
  // be overridden by env.js

  // API url
  public serverUrl = '';
  public openidUrl = '';
  public openidRealmsUrlPrefix = '';
  public openidRealms = '';
  public openidRealmsUrlSuffix = '';
  public openidAuthEndpoint = '';
  public openidTokenEndpoint = '';
  public openidClientId = '';
  public openidBasicAuth = '';

  // Whether or not to enable debug mode
  public enableDebug = true;

  constructor() {
  }

}
