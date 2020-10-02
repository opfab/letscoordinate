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

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'kpiDataTypeFullName'
})
export class KpiDataTypeFullNamePipe implements PipeTransform {

  kpiDataTypeFullNameMap = new Map();

  constructor() {
    this.kpiDataTypeFullNameMap.set('GP', 'Global performance KPIs');
    this.kpiDataTypeFullNameMap.set('BP', 'Business process KPIs');
  }

  transform(value: any, ...args: any[]): any {
    if(value && this.kpiDataTypeFullNameMap.get(value)){
      return this.kpiDataTypeFullNameMap.get(value);
    }
    return value;
  }

}
