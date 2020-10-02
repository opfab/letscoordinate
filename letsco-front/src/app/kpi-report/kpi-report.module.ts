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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {KpiReportConfigComponent} from './kpi-report-config/kpi-report-config.component';
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {KpiReportComponent} from './kpi-report/kpi-report.component';
import {RouterModule} from "@angular/router";
import {CoreModule} from "../core/core.module";
import {BarChartComponent} from './bar-chart/bar-chart.component';
import {ChartsModule} from "ng2-charts";


@NgModule({
  declarations: [
      KpiReportConfigComponent,
      KpiReportComponent,
      BarChartComponent
  ],
  imports: [
      CommonModule,
      CoreModule,
      RouterModule,
      FormsModule,
      NgbModule,
      ChartsModule
  ]
})
export class KpiReportModule { }
