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

import {Component, OnInit} from '@angular/core';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';
import {KpiReportService} from "../../core/services/kpi-report.service";
import {Rsc} from "../../core/models/rsc.model";
import {RscService} from "../../core/models/rsc-service.model";
import {KpiDataType} from "../../core/models/kpi-data-type.model";
import {Router} from "@angular/router";

@Component({
  selector: 'app-kpi-report-config',
  templateUrl: './kpi-report-config.component.html',
  styleUrls: ['./kpi-report-config.component.scss']
})
export class KpiReportConfigComponent implements OnInit {

  rscs: Rsc[] = [];
  rscServices: RscService[] = [];
  kpiDataTypes: KpiDataType[] = [];
  selectedRsc: Rsc;
  selectedRscService: RscService;
  selectedKpiDataType: KpiDataType;

  startModel: NgbDateStruct;
  endModel: NgbDateStruct;

  constructor(private kpiReportService: KpiReportService,
              private router: Router) { }

  ngOnInit() {
    const currentYear: number = new Date().getFullYear();
    this.startModel = {year: currentYear, month: 1, day: 1};
    this.endModel = {year: currentYear, month: 12, day: 31};

    this.kpiReportService.initConfigData();

    this.kpiReportService.rscSubject.subscribe(
        (data) => {
          if (data && data.length > 0) {
            this.rscs.push(new Rsc("ALL", "All"));
            data.forEach(datum => this.rscs.push(datum));
            this.selectedRsc = this.rscs[0];
          }
        }
    );
    this.kpiReportService.rscServiceSubject.subscribe(
        (data) => {
          if (data && data.length > 0) {
            data.forEach(datum => this.rscServices.push(datum));
            this.selectedRscService = this.rscServices[0];
          }
        }
    );
    this.kpiReportService.kpiDataTypeSubject.subscribe(
        (data) => {
          if (data && data.length > 0) {
            this.kpiDataTypes.push(new KpiDataType("ALL", "All"));
            data.forEach(datum => this.kpiDataTypes.push(datum));
            this.selectedKpiDataType = this.kpiDataTypes[0];
          }
        }
    );
  }

  isValidForm(): boolean {
    return !this.selectedRsc || !this.selectedRscService || !this.selectedKpiDataType
        || this.startModel == null || this.endModel == null;
  }

  onSubmit() {
    this.kpiReportService.getReportingData (
        this.startModel,
        this.endModel,
        this.selectedRsc,
        this.selectedRscService,
        this.selectedKpiDataType
    );
    this.router.navigate(['/kpi-report'])
  }

}
