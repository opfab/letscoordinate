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
  name: 'alphabeticNumeral'
})
export class AlphabeticNumeralPipe implements PipeTransform {

  transform(value: number, ...args: any[]): any {
    return this.numberToAlphabeticNumeral(value);
  }

  private numberToAlphabeticNumeral(num: number) {
    let s = '', t;

    while (num > 0) {
      t = (num - 1) % 26;
      s = String.fromCharCode(65 + t) + s;
      num = (num - t)/26 | 0;
    }
    return s || '';
  }

}
