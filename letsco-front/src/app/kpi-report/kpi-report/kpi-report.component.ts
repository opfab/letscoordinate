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
import {RscKpiReportData} from "../../core/models/rsc-kpi-report-data.model";
import {KpiReportService} from "../../core/services/kpi-report.service";
import {Router} from "@angular/router";
import {saveAs} from "file-saver";
import {HttpResponse} from "@angular/common/http";
import {ReportTypeEnum} from "../../core/enums/report-type-enum";
import {DatePipe} from "@angular/common";
import {RomanNumeralPipe} from "../../core/pipes/roman-numeral.pipe";
import {KpiDataTypeFullNamePipe} from "../../core/pipes/kpi-data-type-full-name.pipe";
import * as jsPDF from 'jspdf';
import {ThemeService} from "../../core/services/theme.service";
import {DataGranularityEnum} from "../../core/enums/data-granularity-enum";

const pdfConstants = {
  logoLetsco: {
    _x: 10,
    _y: 285
  },
  pageNum: {
    _x: 100,
    _y: 290
  },
  graph: {
    _x: 13,
    width: 180,
    height: 45
  },
  layout: {
    _x_title1: 15,
    _x_title2: 25,
    _x_text1: 10,
    _x_text2: 13,
    _x_text3: 35,
    _x_max: 100,
    _y_min: 10,
    _y_max: 276,
    titleLineHeight: 7,
    textLineHeight: 5,
    maxFirstPageTitleWidth: 140
  }
};

@Component({
  selector: 'app-kpi-report',
  templateUrl: './kpi-report.component.html',
  styleUrls: ['./kpi-report.component.scss']
})
export class KpiReportComponent implements OnInit {

  rscKpiReportData: RscKpiReportData;
  dataUrlLogoLetsco: string;
  noDataFoundMessage: string = 'No data for this selected granularity';

  constructor(private kpiReportService: KpiReportService,
              private datePipe: DatePipe,
              private romanNumeral: RomanNumeralPipe,
              private kpiDataTypeFullName: KpiDataTypeFullNamePipe,
              private router: Router,
              private themeService: ThemeService) {}

  ngOnInit() {
    this.themeService.initWithTheme(this.themeService.currentThemeCode);
    this.rscKpiReportData = this.kpiReportService.rscKpiReportData;
    if (!this.rscKpiReportData)
      this.router.navigate(['/kpi-report-config']);
  }

  get textareaBgColor() {
    return this.themeService.currentTheme.textComponentBgColor;
  }

  get textareaColor() {
    return this.themeService.currentTheme.textComponentColor;
  }

  generateExcel() {
    this.generateReport(ReportTypeEnum.EXCEL);
  }

  private generateReport(reportTypeEnum: ReportTypeEnum) {
    this.kpiReportService.downloadRscKpiReport(reportTypeEnum)
        .then((response: HttpResponse<Blob>) => {
          let fileName = response.headers.get('content-disposition').split(';')[1].split('filename')[1].split('=')[1].trim();
          saveAs(response.body, fileName);
        });
  }

  generatePdf() {
    let pdf = new jsPDF('p', 'mm', 'a4');
    let pageNum: number = 1;
    this.dataUrlLogoLetsco = null;

    // summary page
    this.addPdfFirstPageTitle(pdf);
    this.generateReportContent(pdf, pageNum, true);
    this.addPdfPageFooter(pdf, pageNum);

    // content pages
    this.generateReportContent(pdf, pageNum, false);

    // file generation
    pdf.save(`${this.kpiReportService.reportFileName}.pdf`);
  }

  addPdfPageFooter(pdf: jsPDF, pageNum: number) {
    if (this.dataUrlLogoLetsco === null) {
      const urlLogoLetsco = document.getElementById('logoLetsco') as HTMLImageElement;
      let canvasLetsco = document.createElement('canvas');
      canvasLetsco.width = urlLogoLetsco.naturalWidth; // or 'width' if you want a special/scaled size
      canvasLetsco.height = urlLogoLetsco.naturalHeight;
      canvasLetsco.getContext('2d').drawImage(urlLogoLetsco, 0, 0);
      this.dataUrlLogoLetsco = canvasLetsco.toDataURL();
    }
    pdf.addImage(this.dataUrlLogoLetsco, 'PNG', pdfConstants.logoLetsco._x, pdfConstants.logoLetsco._y, 55, 7, '','FAST');

    pdf.setFontSize(10);
    pdf.setFont('times', 'normal');
    pdf.text(pdfConstants.pageNum._x, pdfConstants.pageNum._y, pageNum.toString());
  }

  addPdfFirstPageTitle(pdf: jsPDF) {
    let _y = pdfConstants.layout._y_min;
    pdf.setFont('times', 'bold');
    pdf.setFontSize(14);
    let textToCenter = this.selectedEntityType + ' KPIs reporting - ' + this.rscKpiReportData.submittedForm.rscService.name + ' service';
    let textParam = this.getOffsetForTextToCenter(pdf, textToCenter);
    pdf.text(textParam.offset, _y+=10, textToCenter);

    pdf.setFontSize(10);
    if (this.isDailyView) {
      textToCenter = 'From: ' + this.datePipe.transform(this.rscKpiReportData.submittedForm.startDate, 'fullDate');
      textParam = this.getOffsetForTextToCenter(pdf, textToCenter);
      pdf.text(textParam.offset, _y+=10, textToCenter);
      textToCenter = 'To: ' + this.datePipe.transform(this.rscKpiReportData.submittedForm.endDate, 'fullDate');
      textParam = this.getOffsetForTextToCenter(pdf, textToCenter);
      pdf.text(textParam.offset, _y+=7, textToCenter);
    } else {
      textToCenter = 'From  ' + this.rscKpiReportData.submittedForm.startDate.getFullYear() + '  To  ' + this.rscKpiReportData.submittedForm.endDate.getFullYear();
      textParam = this.getOffsetForTextToCenter(pdf, textToCenter);
      pdf.text(textParam.offset, _y+=10, textToCenter);
    }

    _y += 3;
    textToCenter = this.selectedEntityType + ': ' + this.rscsOrRegionsToString;
    textParam = this.getOffsetForTextToCenter(pdf, textToCenter);
    let splitedText = pdf.splitTextToSize(textToCenter, textParam.textWidth);
    for (let i = 0, length = splitedText.length; i < length; i++) {
      if (i < length-1)
        pdf.text(textParam.offset, _y+=7, splitedText[i]);
      else
        pdf.text(this.getOffsetForTextToCenter(pdf, splitedText[i]).offset, _y+=7, splitedText[i]);
    }
  }

  private getOffsetForTextToCenter(pdf: jsPDF, textToCenter: string) : {offset: number, textWidth: number} {
    let pdfPageWidth = parseInt(pdf.internal.pageSize.width);
    let textWidth = Math.min(
        pdf.getStringUnitWidth(textToCenter) * pdf.internal.getFontSize() / pdf.internal.scaleFactor,
        pdfConstants.layout.maxFirstPageTitleWidth
    );
    let offset = (pdfPageWidth - textWidth) / 2;
    return {
      offset: Math.round(offset),
      textWidth: Math.ceil(textWidth)
    };
  }

  generateReportContent(pdf: jsPDF, pageNum: number, isSummaryPage: boolean) {
    let _y : number = (isSummaryPage === true ? 80 : pdfConstants.layout._y_min);
    for (let [i,rscKpiTypedDatum] of this.rscKpiReportData.rscKpiTypedData.entries()) {
      if (isSummaryPage === false) {
        // The BP and GP global titles should be in separate pages
        _y = this.initNewPdfPage(pdf, pageNum+=1);
      }
      pdf.setFont('times', 'bold');
      pdf.setFontSize(10);
      pdf.text(pdfConstants.layout._x_title1, _y += 10, `${this.romanNumeral.transform(i + 1)}. ${this.kpiDataTypeFullName.transform(rscKpiTypedDatum.type)}`);
      for (let [j,rscKpi] of rscKpiTypedDatum.rscKpis.entries()) {
        if ((rscKpi.data[0].kpiChartOptions.hasData() && _y + pdfConstants.graph.height > pdfConstants.layout._y_max)
            || (!rscKpi.data[0].kpiChartOptions.hasData() && _y + pdfConstants.layout.titleLineHeight + pdfConstants.layout.textLineHeight > pdfConstants.layout._y_max)) {
          _y = this.initNewPdfPage(pdf, pageNum+=1);
        }
        pdf.setFont('times', 'bold');
        pdf.setFontSize(10);
        pdf.text(pdfConstants.layout._x_title2, _y += pdfConstants.layout.titleLineHeight, `${j+1}. KPI ${rscKpi.fullName}`);
        if (isSummaryPage === false) {
          for (let [k,rscKpiDatum] of rscKpi.data.entries()) {
            if (rscKpiDatum.kpiChartOptions.hasData()) {
              // drawing graph
              let kpiGraphId = rscKpi.code + '-graph' + k;
              _y += pdfConstants.layout.textLineHeight;
              let dataUrl = (document.getElementById(kpiGraphId) as HTMLCanvasElement).toDataURL();
              if (this.themeService.currentThemeCode === 'NIGHT') {
                pdf.setFillColor(52, 57, 64);
                pdf.rect(pdfConstants.graph._x, _y, pdfConstants.graph.width, pdfConstants.graph.height, 'F');
              }
              pdf.addImage(dataUrl, 'PNG', pdfConstants.graph._x, _y, pdfConstants.graph.width, pdfConstants.graph.height, '', 'FAST');

              // drawing comment
              _y += pdfConstants.graph.height;
              let comment = rscKpiDatum.comment;
              let wrapWidth = 180;
              if ((comment.length > 0 && _y + 2 * pdfConstants.layout.titleLineHeight > pdfConstants.layout._y_max)
                  || (comment.length === 0 && _y + pdfConstants.layout.titleLineHeight > pdfConstants.layout._y_max)) {
                // this check is to avoid writing the word "Comment:" in the bottom of the page without it's content when exists,
                // the should been written together in the top of a new page
                _y = this.initNewPdfPage(pdf, pageNum += 1);
              }
              pdf.setFontSize(8);
              pdf.setFont("times", "bold");
              pdf.text(pdfConstants.layout._x_text2, _y += pdfConstants.layout.titleLineHeight, "Comment: ");
              pdf.setFont("times", "normal");
              if (comment.length > 0) {
                let splitText = pdf.splitTextToSize(comment, wrapWidth);
                for (let i = 0, length = splitText.length; i < length; i++) {
                  pdf.text(splitText[i], pdfConstants.layout._x_text2, _y += pdfConstants.layout.textLineHeight);
                  if (_y > pdfConstants.layout._y_max) {
                    _y = this.initNewPdfPage(pdf, pageNum += 1);
                  }
                }
              }
            } else {
              pdf.setFontSize(8);
              pdf.setFont("times", "normal");
              pdf.text(pdfConstants.layout._x_text3, _y += pdfConstants.layout.titleLineHeight, this.noDataFoundMessage);
            }
          }
        }
      }
    }
  }

  initNewPdfPage(pdf: jsPDF, pageNum: number) : number {
    pdf.addPage();
    this.addPdfPageFooter(pdf, pageNum);
    return pdfConstants.layout._y_min;
  }

  get rscsOrRegionsToString() : string {
    if (this.kpiReportService.rscKpiReportData) {
      let rscs = this.kpiReportService.rscKpiReportData.submittedForm.rscs;
      let regions = this.kpiReportService.rscKpiReportData.submittedForm.regions;
      if (this.isDailyView === true) {
        return rscs.length > 0 ? rscs[0].name : regions[0].name;
      } else {
        return rscs.length > 0 ? rscs.map(r => r.name).join(", ") : regions.map(r => r.name).join(", ");
      }
    }
    return ""
  }

  get isDailyView() : boolean {
    return this.kpiReportService.rscKpiReportData.submittedForm.dataGranularity === DataGranularityEnum.DAILY
  }

  get isRscsSelected() : boolean {
    return this.kpiReportService.rscKpiReportData.submittedForm.rscs && this.kpiReportService.rscKpiReportData.submittedForm.rscs.length > 0;
  }

  get selectedEntityType(): string {
    return this.isRscsSelected === true ? 'RSC' : 'Region';
  }

}
