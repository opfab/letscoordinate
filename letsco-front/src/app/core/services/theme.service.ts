/*
 * Copyright (c) 2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

import {Injectable} from '@angular/core';
import {ChartOptions} from 'chart.js';
import {Theme} from "../models/theme.model";

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  themeMap: Map<string, Theme> = new Map<string, Theme>([
      ['DAY', {color: 'black', bgColor:'white', gridLinesColor: 'rgba(0, 0, 0, 0.1)', textComponentColor: '#343940', textComponentBgColor: 'white'}],
      ['NIGHT', {color: '#e0e0e0', bgColor:'#343940', gridLinesColor: 'rgba(255,255,255,0.1)', textComponentColor: '#343940', textComponentBgColor: '#e0e0e0'}]
  ]);
  currentThemeCode : string = 'NIGHT';

  get currentTheme() : Theme {
    return this.themeMap.get(this.currentThemeCode);
  }

  initWithTheme(themeCode: string) {
    if (['DAY', 'NIGHT'].includes(themeCode)) {
      this.currentThemeCode = themeCode;
      document.body.style.backgroundColor = this.currentTheme.bgColor;
      document.body.style.color = this.currentTheme.color;
      Array.from(document.getElementsByClassName('popover-header')).forEach(e => (e as HTMLTitleElement).style.color = this.currentTheme.bgColor);
      Array.from(document.getElementsByTagName('hr')).forEach(e => e.style.backgroundColor = this.currentTheme.color);
    } else {
      console.warn('Unable to init page with theme: unknown theme \'' + themeCode + '\'!')
    }
  }

  updateChartColors(options: ChartOptions) {
    if (options) {
      options.legend = {
        labels: {
          fontColor: this.currentTheme.color
        }
      };

      options.scales.xAxes = [{
        ticks: {fontColor: this.currentTheme.color},
        gridLines: {
          color: this.currentTheme.gridLinesColor,
          zeroLineColor: this.currentTheme.gridLinesColor
        }
      }];

      options.scales.yAxes[0].ticks = {
        ...options.scales.yAxes[0].ticks,
        ...{fontColor: this.currentTheme.color}
      };
      options.scales.yAxes[0].gridLines = {
        color: this.currentTheme.gridLinesColor,
        zeroLineColor: this.currentTheme.gridLinesColor
      };
    }
  }

}
