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

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {KpiReportConfigComponent} from "./kpi-report/kpi-report-config/kpi-report-config.component";
import {KpiReportComponent} from "./kpi-report/kpi-report/kpi-report.component";


const routes: Routes = [
  {path: '', component: KpiReportConfigComponent},
  {path: 'kpi-report-config', component: KpiReportConfigComponent},
  {path: 'kpi-report', component: KpiReportComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
