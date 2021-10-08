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

import {Label} from "ng2-charts";
import {ChartDataSets, ChartOptions, ChartType} from "chart.js";
import {Color} from "ng2-charts/lib/color";

const lineChartColors: Color[] = [
    {
        borderColor: '#4F81BD',
        backgroundColor: 'transparent',
    },
];
const lineChartOptions: ChartOptions = {
    elements: {
        line: {
            tension: 0
        }
    },
    responsive: true,
    scales: {
        yAxes: [{
            ticks: {
                beginAtZero: true,
                stepSize: 1,
                maxTicksLimit: 10
            }
        }]
    }
};
const barChartOptions: ChartOptions = {
    responsive: true,
    scales: {
        yAxes: [{
            ticks: {
                beginAtZero: true,
                stepSize: 1,
                maxTicksLimit: 10
            }
        }]
    }
};

export class KpiChartOptions {
    chartType: ChartType;
    chartOptions: ChartOptions;
    chartLabels: Label[] = [];
    chartDataSets: ChartDataSets[] = [];
    chartLegend: boolean = true;
    chartHeight: number = 75;
    chartPlugins: any[] = [];
    chartColors: Color[];

    constructor(chartType: ChartType) {
        this.chartType = chartType;
        if (chartType === 'line') {
            this.chartOptions = lineChartOptions;
            this.chartColors = lineChartColors;
        } else if (chartType === 'bar') {
            this.chartOptions = barChartOptions;
        }
    }

    hasData(): boolean {
        for (let i = 0 ; i < this.chartDataSets.length ; i++) {
            if (this.chartDataSets[i]) {
                for (let j = 0; j < this.chartDataSets[i].data.length; j++) {
                    if (this.chartDataSets[i].data[j] && this.chartDataSets[i].data[j] !== 0)
                        return true;
                }
            }
        }
        return false;
    }

}
