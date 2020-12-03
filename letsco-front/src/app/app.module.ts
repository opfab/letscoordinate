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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { KpiReportModule } from './kpi-report/kpi-report.module';
import {CoreModule} from "./core/core.module";
import {RouterModule} from "@angular/router";
import {EnvServiceProvider} from "./core/services/env.service.provider";
import {DatePipe} from "@angular/common";
import {RomanNumeralPipe} from "./core/pipes/roman-numeral.pipe";
import {KpiDataTypeFullNamePipe} from "./core/pipes/kpi-data-type-full-name.pipe";
import { LoginComponent } from './login/login.component';
import {ChartsModule} from "ng2-charts";

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent
  ],
  imports: [
      BrowserModule,
      AppRoutingModule,
      RouterModule,
      KpiReportModule,
      CoreModule,
      ChartsModule
  ],
  providers: [
      EnvServiceProvider,
      DatePipe,
      RomanNumeralPipe,
      KpiDataTypeFullNamePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
