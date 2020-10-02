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

import {Injectable} from "@angular/core";
import {Adapter} from "../utils/adapter";
import {RscKpiTemporalData, RscKpiTemporalDataAdapter} from "./rsc-kpi-temporal-data.model";
import {KpiChartOptions} from "./kpi-chart-options.model";
import {formatDate} from "@angular/common";

export class RscKpiData {
    constructor(public label: string,
                public comment: string,
                public kpiChartOptions: KpiChartOptions,
                public kpiChartId: string,
                public rscKpiTemporalData: RscKpiTemporalData[]) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RscKpiDataAdapter implements Adapter<RscKpiData>{
    constructor(private rscKpiTemporalDataAdapter: RscKpiTemporalDataAdapter) {}

    adapt(entry: any, ...extraParams: any[]): RscKpiData {
        let key = entry[0]; // used later as label for graph
        let value = entry[1]; // graph values
        let kpiSubtypeCode = extraParams[1]; // GPx or BPx (while x in [1..n])
        let rscKpiTemporalData: RscKpiTemporalData[] = [];
        if(Array.from(value).length === 0) {
            rscKpiTemporalData.push(this.rscKpiTemporalDataAdapter.adapt({details:[{}]}));
        } else {
            value.forEach(rscKpiTmprlData => rscKpiTemporalData.push(this.rscKpiTemporalDataAdapter.adapt(rscKpiTmprlData)));
        }
        let chartOptions = new KpiChartOptions(extraParams[0][0][1][kpiSubtypeCode].graphType);
        // set the main graph legend
        chartOptions.chartDataSets[0].label = key;
        // use of a temporary data array to be able to delete found elements (to improve searching performance)
        let tmpRscKpiTemporalData = rscKpiTemporalData;
        let startDate = new Date(extraParams[0][0][0].startDate);
        let endDate = new Date(extraParams[0][0][0].endDate);
        let graphDatePattern = 'dd/MM/yyyy';
        // loop on all dates between the startDate and the endDate
        for (let date = startDate; date <= endDate; date.setDate(date.getDate() + 1)) {
            // set dates as labels for the x axis of the chart
            chartOptions.chartLabels.push(formatDate(date, graphDatePattern, 'en_US', '+0000'));
            let tmpDataIndex: Number = null;
            // search for the appropriate data from the temporary array
            for(let [index, tmpData] of Object.entries(tmpRscKpiTemporalData)) {
                if (this.dateEquals(date, new Date(tmpData.timestamp))) {
                    // set found data for the main graph
                    chartOptions.chartDataSets[0].data.push(tmpData.value);
                    // index to be used to remove the found data from the temporary array
                    tmpDataIndex = parseInt(index);
                    break;
                }
            }
            if(tmpDataIndex !== null) // remove found element by its index
                tmpRscKpiTemporalData.splice(tmpDataIndex.valueOf(), 1);
            else // push 0 as value when no data found for any date
                chartOptions.chartDataSets[0].data.push(0);
        }
        return new RscKpiData (
            key,
            "",
            chartOptions,
            extraParams[2], // kpiGraphId (e.g: BP1-graph0, GP1-graph1, ...)
            rscKpiTemporalData
        );
    }

    dateEquals(date1: Date, date2: Date): boolean {
        return date1 && date2
            && date1.getDate() === date2.getDate()
            && date1.getMonth() === date2.getMonth()
            && date1.getFullYear() === date2.getFullYear();
    }
}
