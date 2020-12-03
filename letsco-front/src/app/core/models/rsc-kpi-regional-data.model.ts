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

import {Injectable} from "@angular/core";
import {Adapter} from "../utils/adapter";

export class RscKpiRegionalData {
    constructor(public regionName: string,
                public value: number) {
    }
}


@Injectable({
    providedIn: 'root'
})
export class RscKpiRegionalDataAdapter implements Adapter<RscKpiRegionalData>{
    adapt(item: any): RscKpiRegionalData {
        return new RscKpiRegionalData(
            item.name,
            item.details[0].value
        );
    }
}

