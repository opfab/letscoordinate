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

import {RscKpiData, RscKpiDataAdapter} from "./rsc-kpi-data.model";
import {Injectable} from "@angular/core";
import {Adapter} from "../utils/adapter";
import {DataGranularityEnum} from "../enums/data-granularity-enum";

export class RscKpi {
    constructor(public code: string,
                public name: string,
                public fullName: string,
                public divHidden: boolean,
                public data: RscKpiData[]) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RscKpiAdapter implements Adapter<RscKpi>{
    constructor(private rscKpiDataAdapter: RscKpiDataAdapter) {}

    adapt(entry: any, ...extraParams: any): RscKpi {
        let key = entry[0]; // GPx or BPx (while x in [1..n])
        let value = entry[1]; // Map (key: graph legend, value: list of {timestamp, label, details} objects)
        let rscKpiData: RscKpiData[] = [];
        let kpiSubtype = extraParams[0][1] ? extraParams[0][1][key] : null; // the full name of the GPx or BPx
        let submittedFormData = extraParams[0][0]; // values selected in the config page

        if (submittedFormData.dataGranularity === DataGranularityEnum.DAILY) { // CASE: DAILY VIEW
            if (Array.from(Object.entries(value)).length === 0) {
                rscKpiData.push(this.rscKpiDataAdapter.adapt([kpiSubtype ? kpiSubtype.name : key, []], extraParams, key, key + '-graph0'))
            } else {
                Array.from(Object.entries(value))
                    .forEach((entry, index) => rscKpiData.push(this.rscKpiDataAdapter.adapt(entry, extraParams, key, key + '-graph' + index)));
            }
        } else { // CASE: MULTI-YEAR VIEW
            if (Array.from(Object.entries(value)).length === 0) {
                rscKpiData.push(this.rscKpiDataAdapter.adapt([kpiSubtype ? kpiSubtype.name : key, []], extraParams, key, key + '-graph0'))
            } else {
                rscKpiData.push(this.rscKpiDataAdapter.adapt(entry, extraParams, key, key + '-graph0'));
            }
        }

        return new RscKpi(
            key,
            kpiSubtype && kpiSubtype.name ? kpiSubtype.name : null,
            key + (kpiSubtype && kpiSubtype.name ? " - " + kpiSubtype.name : ""),
            false,
            rscKpiData
        );
    }
}
