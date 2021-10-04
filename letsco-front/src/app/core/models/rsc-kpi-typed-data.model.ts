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

import {RscKpi, RscKpiAdapter} from "./rsc-kpi.model";
import {Injectable} from "@angular/core";
import {Adapter} from "../utils/adapter";

export class RscKpiTypedData {
    constructor(public type: string,
                public rscKpis: RscKpi[]) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RscKpiTypedDataAdapter implements Adapter<RscKpiTypedData>{
    constructor(private rscKpiAdapter: RscKpiAdapter) {}

    adapt(entry: any, ...extraParams: any): RscKpiTypedData {
        let key = entry[0]; // GP ou BP
        let value = entry[1];
        let rscKpis: RscKpi[] = [];
        Array.from(Object.entries(value))
            .forEach(item => rscKpis.push(this.rscKpiAdapter.adapt(item, extraParams)));

        return new RscKpiTypedData(
            key,
            rscKpis.sort((a,b) => a.index - b.index)
        );
    }
}
