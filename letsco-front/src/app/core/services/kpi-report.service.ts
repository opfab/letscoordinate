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

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Subject} from "rxjs";
import {Rsc, RscAdapter} from "../models/rsc.model";
import {RscService, RscServiceAdapter} from "../models/rsc-service.model";
import {KpiDataType, KpiDataTypeAdapter} from "../models/kpi-data-type.model";
import {NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {RscKpiReportData} from "../models/rsc-kpi-report-data.model";
import {RscKpiTypedData, RscKpiTypedDataAdapter} from "../models/rsc-kpi-typed-data.model";
import {EnvService} from "./env.service";
import {KpiSubmittedForm} from "../models/kpi-submitted-form.model";
import {ReportTypeEnum} from "../enums/report-type-enum";
import {AuthService} from "./auth.service";
import {Region, RegionAdapter} from "../models/region.model";
import {ViewTypeEnum} from "../enums/view-type-enum";


@Injectable({
    providedIn: 'root'
})
export class KpiReportService {

    constructor(private authService: AuthService,
                private envService: EnvService,
                private httpClient: HttpClient,
                private rscAdapter: RscAdapter,
                private regionAdapter: RegionAdapter,
                private rscServiceAdapter: RscServiceAdapter,
                private kpiDataTypeAdapter: KpiDataTypeAdapter,
                private rscKpiTypedDataAdapter: RscKpiTypedDataAdapter) {
    };

    URL_REPORTING_CONFIG_DATA = this.envService.serverUrl + '/v1/rsc-kpi-report/config-data';
    URL_REPORTING_DATA = this.envService.serverUrl + '/v1/rsc-kpi-report/kpis';
    URL_DOWNLOAD_EXCEL_REPORT = this.envService.serverUrl + '/v1/rsc-kpi-report/download/excel';
    URL_DOWNLOAD_PDF_REPORT = this.envService.serverUrl + '/v1/rsc-kpi-report/download/pdf';

    private rscs: Rsc[] = [];
    rscSubject = new Subject<Rsc[]>();

    private regions: Region[] = [];
    regionSubject = new Subject<Region[]>();

    private rscServices: RscService[] = [];
    rscServiceSubject = new Subject<RscService[]>();

    private kpiDataTypes: KpiDataType[] = [];
    kpiDataTypeSubject = new Subject<KpiDataType[]>();

    rscKpiReportData: RscKpiReportData;

    reportFileName: string;

    emitRsc() {
        this.rscSubject.next(this.rscs);
    }

    emitRegion() {
        this.regionSubject.next(this.regions);
    }

    emitRscService() {
        this.rscServiceSubject.next(this.rscServices);
    }

    emitKpiDataType() {
        this.kpiDataTypeSubject.next(this.kpiDataTypes);
    }

    initConfigData() {
        this.rscs = [];
        this.regions = [];
        this.rscServices = [];
        this.kpiDataTypes = [];
        this.httpClient.get(this.URL_REPORTING_CONFIG_DATA, this.authService.tokenHeader)
            .subscribe(
                (res: any) => {
                    res.rscs.forEach(item => this.rscs.push(this.rscAdapter.adapt(item)));
                    this.emitRsc();
                    res.regions.forEach(item => this.regions.push(this.regionAdapter.adapt(item)));
                    this.emitRegion();
                    res.rscServices.forEach(item => this.rscServices.push(this.rscServiceAdapter.adapt(item)));
                    this.emitRscService();
                    res.kpiDataTypes.forEach(item => this.kpiDataTypes.push(this.kpiDataTypeAdapter.adapt(item)));
                    this.emitKpiDataType();
                }
            );
    }

    getReportingData(selectedViewType: ViewTypeEnum ,startModel: NgbDateStruct, endModel: NgbDateStruct, startYear: number, endYear: number,
                     rsc: Rsc, rscs: Rsc[], region: Region, regions: Region[], rscService: RscService, kpiDataType: KpiDataType) {
        let rscKpiTypedData: RscKpiTypedData[] = [];
        const startDate = selectedViewType === ViewTypeEnum.DAILY
            ? new Date(Date.UTC(startModel.year, startModel.month - 1, startModel.day))
            : new Date(Date.UTC(startYear, 0, 1));
        const endDate = selectedViewType === ViewTypeEnum.DAILY
            ? new Date(Date.UTC(endModel.year, endModel.month - 1, endModel.day))
            : new Date(Date.UTC(endYear, 11, 31));
        const submittedForm = {
            viewTypeEnum: selectedViewType,
            startDate: startDate,
            endDate: endDate,
            rscs: (selectedViewType === ViewTypeEnum.DAILY ? (rsc ? [rsc] : []) : rscs),
            regions: (selectedViewType === ViewTypeEnum.DAILY ? (region ? [region] : []) : regions),
            rscService: rscService,
            kpiDataType: kpiDataType
        }
        const requestBody = {
            viewTypeEnum: selectedViewType,
            startDate: startDate,
            endDate: endDate,
            rscCodes: (selectedViewType === ViewTypeEnum.DAILY ? (rsc ? [rsc.eicCode] : []) : rscs.map(rsc => rsc.eicCode)),
            regionCodes: (selectedViewType === ViewTypeEnum.DAILY ? (region ? [region.eicCode] : []) : regions.map(region => region.eicCode)),
            rscServiceCode: rscService.code,
            kpiDataTypeCode: kpiDataType.code
        };
        this.httpClient.post(this.URL_REPORTING_DATA, requestBody, this.authService.tokenHeader)
            .subscribe(
            (res: any) => {
                this.reportFileName = res.reportFileName;
                if ((kpiDataType.code === 'ALL' || kpiDataType.code === 'GP') && res.rscKpiTypedDataMap.GP)
                    rscKpiTypedData.push(this.rscKpiTypedDataAdapter.adapt(["GP", res.rscKpiTypedDataMap.GP], submittedForm, res.rscKpiSubtypedDataMap));
                if ((kpiDataType.code === 'ALL' || kpiDataType.code === 'BP') && res.rscKpiTypedDataMap.BP)
                    rscKpiTypedData.push(this.rscKpiTypedDataAdapter.adapt(["BP", res.rscKpiTypedDataMap.BP], submittedForm, res.rscKpiSubtypedDataMap));
            }
        );
        this.rscKpiReportData = new RscKpiReportData(
            new KpiSubmittedForm(
                selectedViewType,
                startDate,
                endDate,
                (selectedViewType === ViewTypeEnum.DAILY ? (rsc ? [rsc] : []) : rscs),
                (selectedViewType === ViewTypeEnum.DAILY ? (region ? [region] : []) : regions),
                rscService,
                kpiDataType),
            rscKpiTypedData
        );
    }

    downloadRscKpiReport(reportTypeEnum: ReportTypeEnum) {
        const requestBody = {
            viewTypeEnum: this.rscKpiReportData.submittedForm.viewTypeEnum,
            startDate: this.rscKpiReportData.submittedForm.startDate,
            endDate: this.rscKpiReportData.submittedForm.endDate,
            rscCodes: this.rscKpiReportData.submittedForm.rscs.map(r=>r.eicCode),
            regionCodes: this.rscKpiReportData.submittedForm.regions.map(r=>r.eicCode),
            rscServiceCode: this.rscKpiReportData.submittedForm.rscService.code,
            kpiDataTypeCode: this.rscKpiReportData.submittedForm.kpiDataType.code
        };
        return new Promise((resolve, reject) => {
            this.httpClient.post(reportTypeEnum === ReportTypeEnum.EXCEL? this.URL_DOWNLOAD_EXCEL_REPORT : this.URL_DOWNLOAD_PDF_REPORT,
                requestBody, {headers: this.authService.tokenHeader.headers, responseType: 'blob', observe: 'response'})
                    .subscribe((response: any) => {resolve(response)}, reject)
            }
        );
    }

}
