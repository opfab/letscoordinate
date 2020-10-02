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


@Injectable({
    providedIn: 'root'
})
export class KpiReportService {

    constructor(private envService: EnvService,
                private httpClient: HttpClient,
                private rscAdapter: RscAdapter,
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

    private rscServices: RscService[] = [];
    rscServiceSubject = new Subject<RscService[]>();

    private kpiDataTypes: KpiDataType[] = [];
    kpiDataTypeSubject = new Subject<KpiDataType[]>();

    rscKpiReportData: RscKpiReportData;

    emitRsc() {
        this.rscSubject.next(this.rscs);
    }

    emitRscService() {
        this.rscServiceSubject.next(this.rscServices);
    }

    emitKpiDataType() {
        this.kpiDataTypeSubject.next(this.kpiDataTypes);
    }

    initConfigData() {
        this.rscs = [];
        this.rscServices = [];
        this.kpiDataTypes = [];
        this.httpClient.get(this.URL_REPORTING_CONFIG_DATA)
            .subscribe(
                (res: any) => {
                    res.rscs.forEach(item => this.rscs.push(this.rscAdapter.adapt(item)));
                    this.emitRsc();
                    res.rscServices.forEach(item => this.rscServices.push(this.rscServiceAdapter.adapt(item)));
                    this.emitRscService();
                    res.kpiDataTypes.forEach(item => this.kpiDataTypes.push(this.kpiDataTypeAdapter.adapt(item)));
                    this.emitKpiDataType();
                }
            );
    }

    getReportingData(startModel: NgbDateStruct, endModel: NgbDateStruct, rsc: Rsc, rscService: RscService, kpiDataType: KpiDataType) {
        let rscKpiTypedData: RscKpiTypedData[] = [];
        const startDate = new Date(Date.UTC(startModel.year, startModel.month - 1, startModel.day));
        const endDate = new Date(Date.UTC(endModel.year, endModel.month - 1, endModel.day));
        const requestBody = {
            startDate: startDate,
            endDate: endDate,
            rscCode: rsc.eicCode,
            rscServiceCode: rscService.code,
            kpiDataTypeCode: kpiDataType.code
        };
        this.httpClient.post(this.URL_REPORTING_DATA, requestBody)
            .subscribe(
            (res: any) => {
                if (kpiDataType.code === 'ALL' || kpiDataType.code === 'GP')
                    rscKpiTypedData.push(this.rscKpiTypedDataAdapter.adapt(["GP", res.rscKpiTypedDataMap.GP], requestBody, res.rscKpiSubtypedDataMap));
                if (kpiDataType.code === 'ALL' || kpiDataType.code === 'BP')
                    rscKpiTypedData.push(this.rscKpiTypedDataAdapter.adapt(["BP", res.rscKpiTypedDataMap.BP], requestBody, res.rscKpiSubtypedDataMap));
            }
        );
        this.rscKpiReportData = new RscKpiReportData(
            new KpiSubmittedForm(startDate, endDate, rsc, rscService, kpiDataType),
            rscKpiTypedData
        );
    }

    downloadRscKpiReport(reportTypeEnum: ReportTypeEnum) {
        const requestBody = {
            startDate: this.rscKpiReportData.submittedForm.startDate,
            endDate: this.rscKpiReportData.submittedForm.endDate,
            rscCode: this.rscKpiReportData.submittedForm.rsc.eicCode,
            rscServiceCode: this.rscKpiReportData.submittedForm.rscService.code,
            kpiDataTypeCode: this.rscKpiReportData.submittedForm.kpiDataType.code
        };
        return new Promise((resolve, reject) => {
            this.httpClient.post(reportTypeEnum === ReportTypeEnum.EXCEL? this.URL_DOWNLOAD_EXCEL_REPORT : this.URL_DOWNLOAD_PDF_REPORT,
                requestBody, {responseType: 'blob', observe: 'response'})
                    .subscribe((response: any) => {resolve(response)}, reject)
            }
        );
    }

}
