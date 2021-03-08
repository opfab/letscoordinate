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
import {DataGranularityEnum} from "../enums/data-granularity-enum";
import {RscKpiRegionalDataAdapter} from "./rsc-kpi-regional-data.model";
import ChartDataLabels from 'chartjs-plugin-datalabels';
import * as Chart from "chart.js";

export class RscKpiData {
    constructor(public label: string,
                public comment: string,
                public kpiChartOptions: KpiChartOptions,
                public kpiChartId: string,
                public rscKpiTemporalData: any[]) {
    }
}

@Injectable({
    providedIn: 'root'
})
export class RscKpiDataAdapter implements Adapter<RscKpiData>{
    constructor(private rscKpiTemporalDataAdapter: RscKpiTemporalDataAdapter,
                private rscKpiRegionalDataAdapter: RscKpiRegionalDataAdapter) {}

    adapt(entry: any, ...extraParams: any[]): RscKpiData {
        let kpiSubtypeCode = extraParams[1]; // GPx or BPx (while x in [1..n])
        let submittedFormData = extraParams[0][0][0]; // values selected in the config page
        if (submittedFormData.dataGranularity === DataGranularityEnum.DAILY) { // CASE: DAILY GRANULARITY
            let chartOptions = new KpiChartOptions(extraParams[0][0][1][kpiSubtypeCode].graphType);
            let key, value;
            let rscKpiTemporalData: RscKpiTemporalData[];
            Array.from(Object.entries(entry)).forEach( (entryTmp, index0) => {
                key = entryTmp[1][0]; // used later as label for graph
                value = entryTmp[1][1]; // graph values
                rscKpiTemporalData = [];
                if (Array.from(value).length === 0) {
                    rscKpiTemporalData.push(this.rscKpiTemporalDataAdapter.adapt({details: [{}]}));
                } else {
                    value.forEach(rscKpiTmprlData => rscKpiTemporalData.push(this.rscKpiTemporalDataAdapter.adapt(rscKpiTmprlData)));
                }
                // ChartDataLabels should be unregistered to hide values in DAILY VIEW graphs
                Chart.plugins.unregister(ChartDataLabels);
                // set the main graph legend
                chartOptions.chartDataSets[index0] = { data: [], label: key };
                // use of a temporary data array to be able to delete found elements (to improve searching performance)
                let tmpRscKpiTemporalData = rscKpiTemporalData;
                let startDate = new Date(submittedFormData.startDate);
                let endDate = new Date(submittedFormData.endDate);
                let graphDatePattern = 'dd/MM/yyyy';
                // loop on all dates between the startDate and the endDate
                for (let date = startDate; date <= endDate; date.setUTCDate(date.getUTCDate() + 1)) {
                    // set dates as labels for the x axis of the chart (only once)
                    if (index0 === 0)
                        chartOptions.chartLabels.push(formatDate(date, graphDatePattern, 'en_US', '+0000'));
                    let tmpDataIndex: Number = null;
                    // search for the appropriate data from the temporary array
                    for (let [index1, tmpData] of Object.entries(tmpRscKpiTemporalData)) {
                        if (this.dateEquals(date, new Date(tmpData.timestamp))) {
                            // set found data for the main graph
                            chartOptions.chartDataSets[index0].data.push(tmpData.value);
                            // index to be used to remove the found data from the temporary array
                            tmpDataIndex = parseInt(index1);
                            break;
                        }
                    }
                    if (tmpDataIndex !== null) // remove found element by its index
                        tmpRscKpiTemporalData.splice(tmpDataIndex.valueOf(), 1);
                    else // push 0 as value when no data found for any date
                        chartOptions.chartDataSets[index0].data.push(0);
                }
            });
            return new RscKpiData(
                key,
                "",
                chartOptions,
                extraParams[2], // kpiGraphId (e.g: BP1-graph0, GP1-graph1, ...)
                rscKpiTemporalData
            );
        } else { // CASE: YEARLY GRANULARITY
            let key = entry[0]; // GPx or BPx (while x in [1..n])
            let value = entry[1]; // Map (key: graph legend, value: list of {timestamp, label, details} objects)
            let startYear = new Date(submittedFormData.startDate).getFullYear();
            let endYear = new Date(submittedFormData.endDate).getFullYear();
            let year = startYear;
            let legend_index = 0;
            let chartOptions = new KpiChartOptions('bar');
            // ChartDataLabels should be registered to display values in MULTI-YEAR VIEW graphs
            Chart.plugins.register(ChartDataLabels);
            chartOptions.chartPlugins = [{plugins: [ChartDataLabels]}];
            chartOptions.chartOptions.plugins = {
                datalabels: {
                    align: 'end',
                    anchor: 'end',
                    color: '#888888',
                    font: { size: 11, weight: 'bold' },
                    offset: 0,
                    padding: 0
                }
            }
            let rscsOrRegions = submittedFormData.rscs.length > 0 ? submittedFormData.rscs : submittedFormData.regions;
            rscsOrRegions = rscsOrRegions.sort((o1, o2) => o1.index - o2.index);
            rscsOrRegions.forEach(r => chartOptions.chartLabels.push(r.shortName));

            while(year <= endYear) {
                chartOptions.chartDataSets[legend_index] = { data: Array.from({length:chartOptions.chartLabels.length}).map(x=>null), label: ''+year };
                let temporalValues = value[year];
                if(temporalValues && temporalValues.length > 0){
                    temporalValues = temporalValues.sort((o1, o2)   => new Date(o2.timestamp).getTime() - new Date(o1.timestamp).getTime()); // sort desc by timestamp to get latest value
                    for (let rscIndex=0; rscIndex<rscsOrRegions.length; rscIndex++) {
                        let entityValueFound : boolean = false;
                        for(let k=0 ; k<temporalValues.length; k++) {
                            if(temporalValues[k].details && temporalValues[k].details.length > 0) {
                                let valuesFound = temporalValues[k].details.filter(d => d.eicCode === rscsOrRegions[rscIndex].eicCode);
                                if (valuesFound && valuesFound.length > 0) {
                                    entityValueFound = true;
                                    chartOptions.chartDataSets[legend_index].data[rscIndex] = valuesFound[0].value;
                                    break;
                                }
                            }
                        }
                        if(entityValueFound === false && kpiSubtypeCode.startsWith('GP')) {
                            for(let k=0 ; k<temporalValues.length; k++) {
                                if(temporalValues[k].details && temporalValues[k].details.length > 0) {
                                    let valuesFound = temporalValues[k].details.filter(d => !d.eicCode); // d.eicCode is null or undefined
                                    if (valuesFound && valuesFound.length > 0) {
                                        chartOptions.chartDataSets[legend_index].data[rscIndex] = valuesFound[0].value;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                legend_index++;
                year++;
            }
            return new RscKpiData(
                key,
                "",
                chartOptions,
                extraParams[2], // kpiGraphId (e.g: BP1-graph0, GP1-graph1, ...)
                null
            );
        }
    }

    dateEquals(date1: Date, date2: Date): boolean {
        return date1 && date2
            && date1.getDate() === date2.getDate()
            && date1.getMonth() === date2.getMonth()
            && date1.getFullYear() === date2.getFullYear();
    }
}
