function fn() {

// Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
// Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
// See AUTHORS.txt
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
// SPDX-License-Identifier: MPL-2.0
// This file is part of the Let’s Coordinate project.
    
    var config = { // base config JSON
      opfabUrl: 'http://localhost/',
      opfabPublishCardUrl: 'http://localhost:2102/'
    };

    karate.log('url opfab :' + config.opfabUrl);
    // don't waste time waiting for a connection or if servers don't respond within 5 seconds
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
    
    return config;
  }
