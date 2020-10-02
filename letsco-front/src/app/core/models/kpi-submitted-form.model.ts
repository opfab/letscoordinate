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

import {Rsc} from "./rsc.model";
import {RscService} from "./rsc-service.model";
import {KpiDataType} from "./kpi-data-type.model";

export class KpiSubmittedForm {
    constructor(public startDate: Date,
                public endDate: Date,
                public rsc: Rsc,
                public rscService: RscService,
                public kpiDataType: KpiDataType) {
    }
}
