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
import {Region} from "../../core/models/region.model";
import {DataGranularityEnum} from "../../core/enums/data-granularity-enum";

const PERIOD_INTERVAL = 4;
const MIN_YEAR = 0;
const MAX_YEAR = 9999;
const ALL_RSCS_CODE = 'ALL_RSCS';
const ALL_REGIONS_CODE = 'ALL_REGIONS';
const PAN_EUROPEAN_LABEL = 'Pan-EU';

@Component({
  selector: 'app-kpi-report-config',
  templateUrl: './kpi-report-config.component.html',
  styleUrls: ['./kpi-report-config.component.scss']
})
export class KpiReportConfigComponent implements OnInit {

  rscs: Rsc[] = [];
  regions: Region[] = [];
  rscServices: RscService[] = [];
  kpiDataTypes: KpiDataType[] = [];
  selectedRsc: Rsc;
  selectedRegion: Region;
  selectedRscService: RscService;
  selectedKpiDataType: KpiDataType;

  selectedDataGranularity: DataGranularityEnum;
  startModel: NgbDateStruct;
  endModel: NgbDateStruct;
  startYear: any;
  endYear: any;

  constructor(private kpiReportService: KpiReportService,
              private router: Router) { }

  ngOnInit() {
    const currentYear: number = new Date().getFullYear();
    this.startModel = {year: currentYear, month: 1, day: 1};
    this.endModel = {year: currentYear, month: 12, day: 31};
    this.startYear = currentYear;
    this.endYear = currentYear + PERIOD_INTERVAL;
    this.selectedDataGranularity = DataGranularityEnum.DAILY;

    this.kpiReportService.initConfigData();

    this.kpiReportService.rscSubject.subscribe(
        (data) => {
          if (data && data.length > 0) {
            this.rscs.push(new Rsc(ALL_RSCS_CODE, PAN_EUROPEAN_LABEL, PAN_EUROPEAN_LABEL, 0, true));
            data.forEach(datum => this.rscs.push(datum));
            this.selectedRsc = this.rscs[0];
          }
        }
    );
    this.kpiReportService.regionSubject.subscribe(
        (data) => {
          if (data && data.length > 0) {
            this.regions.push(new Region(ALL_REGIONS_CODE, PAN_EUROPEAN_LABEL, PAN_EUROPEAN_LABEL, 0, false));
            data.forEach(datum => this.regions.push(datum));
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
    return this.selectedRscService
        && this.selectedKpiDataType
        && this.isValidPeriod()
        && this.isValidRscOrRegion();
  }

  private isValidPeriod(): boolean {
    return (this.selectedDataGranularity===DataGranularityEnum.DAILY && this.startModel !== null && this.endModel !== null)
        || (this.selectedDataGranularity===DataGranularityEnum.YEARLY && this.startYear !== null && this.endYear !== null)
  }

  private isValidRscOrRegion(): boolean {
    return (this.selectedDataGranularity===DataGranularityEnum.DAILY && (this.selectedRsc !== null || this.selectedRegion !== null))
        || (this.selectedDataGranularity===DataGranularityEnum.YEARLY && (this.selectedRscs.length > 0 || this.selectedRegions.length > 0))
  }

  onSubmit() {
    this.kpiReportService.getReportingData (
        this.selectedDataGranularity,
        this.startModel,
        this.endModel,
        this.startYear,
        this.endYear,
        this.selectedRsc,
        this.selectedRscs,
        this.selectedRegion,
        this.selectedRegions,
        this.selectedRscService,
        this.selectedKpiDataType
    );
    this.router.navigate(['/kpi-report'])
  }

  isNumeralInput(charCode: any) {
    // 48 is the charCode of 0 and 57 is the charCode of 9
    return charCode >= 48 && charCode <= 57
  }

  checkEndYear() {
    if(this.startYear) {
      if(Number(this.endYear) < Number(this.startYear) || Number(this.endYear) > Number(this.startYear) + PERIOD_INTERVAL) {
        this.endYear = Number(this.startYear) + PERIOD_INTERVAL > MAX_YEAR ? MAX_YEAR : Number(this.startYear) + PERIOD_INTERVAL;
      }
    } else {
      this.checkStartYear();
    }
  }

  checkStartYear() {
    if(this.endYear) {
      if(Number(this.endYear) < Number(this.startYear) || Number(this.endYear) > Number(this.startYear) + PERIOD_INTERVAL) {
        this.startYear = Number(this.endYear) - PERIOD_INTERVAL < MIN_YEAR ? MIN_YEAR : Number(this.endYear) - PERIOD_INTERVAL;
      }
    } else {
      this.checkEndYear();
    }
  }

  checkAllRscs() {
    this.rscs.forEach(rsc => rsc.checked = true);
    this.uncheckAllRegions();
  }

  uncheckAllRscs() {
    this.rscs.forEach(rsc => rsc.checked = false);
  }

  checkAllRegions() {
    this.regions.forEach(region => region.checked = true);
    this.uncheckAllRscs();
  }

  uncheckAllRegions() {
    this.regions.forEach(region => region.checked = false);
  }

  isAllRscsChecked(): boolean {
    return this.rscs.filter(rsc => rsc.checked === false).length === 0;
  }

  isAllRegionsChecked(): boolean {
    return this.regions.filter(region => region.checked === false).length === 0;
  }

  get selectedRscs() {
    return this.rscs.filter(rsc => rsc.checked === true);
  }

  get selectedRegions() {
    return this.regions.filter(region => region.checked === true);
  }

  set selected_rsc(selectedRsc: any) {
    this.selectedRsc = selectedRsc;
    this.selectedRegion = null;
  }

  get selected_rsc() {
    return this.selectedRsc;
  }

  set selected_region(selectedRegion: any) {
    this.selectedRegion = selectedRegion;
    this.selectedRsc = null;
  }

  get selected_region() {
    return this.selectedRegion;
  }

}
