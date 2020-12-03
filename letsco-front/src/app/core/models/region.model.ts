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

export class Region {
    constructor(public eicCode: string,
                public name: string,
                public shortName: string,
                public index: number,
                public checked: boolean) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RegionAdapter implements Adapter<Region>{
    adapt(item: any, ...extraParams: any): Region {
        return new Region(
            item.eicCode,
            item.name,
            item.shortName,
            item.index,
            false
        );
    }
}
