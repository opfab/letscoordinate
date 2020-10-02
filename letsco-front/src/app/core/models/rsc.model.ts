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

import {RscService, RscServiceAdapter} from "./rsc-service.model";
import {Injectable} from "@angular/core";
import {Adapter} from "../utils/adapter";

export class Rsc {
    constructor(public eicCode: string,
                public name: string,
                public rscServices: RscService[]) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RscAdapter implements Adapter<Rsc>{
    constructor(private rscServiceAdapter: RscServiceAdapter) {}

    adapt(item: any, ...extraParams: any): Rsc {
        let rscServices: RscService[] = [];
        item.rscServiceDtos.forEach(rscServ => rscServices.push(this.rscServiceAdapter.adapt(rscServ)));
        return new Rsc(
            item.eicCode,
            item.name,
            rscServices
        );
    }
}
