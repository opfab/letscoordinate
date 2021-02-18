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

import {AfterViewChecked, Component, Input} from '@angular/core';
import {KpiChartOptions} from "../../core/models/kpi-chart-options.model";
import {ThemeService} from "../../core/services/theme.service";
import {KpiReportService} from "../../core/services/kpi-report.service";
import {DataGranularityEnum} from "../../core/enums/data-granularity-enum";

@Component({
  selector: 'app-kpi-chart',
  templateUrl: './kpi-chart.component.html',
  styleUrls: ['./kpi-chart.component.scss'],
})
export class KpiChartComponent implements AfterViewChecked{
  @Input() public kpiChartId: string;
  @Input() public kpiChartOptions: KpiChartOptions;
  @Input() public dataGranularity: DataGranularityEnum;

  constructor(private themeService: ThemeService) {
  }

  ngAfterViewChecked() {
    this.themeService.updateChartColors(this.kpiChartOptions.chartOptions, this.dataGranularity)
  }
}
