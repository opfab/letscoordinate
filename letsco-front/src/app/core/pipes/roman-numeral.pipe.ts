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

const romanMatrix = [
  [1000, 'M'],
  [900, 'CM'],
  [500, 'D'],
  [400, 'CD'],
  [100, 'C'],
  [90, 'XC'],
  [50, 'L'],
  [40, 'XL'],
  [10, 'X'],
  [9, 'IX'],
  [5, 'V'],
  [4, 'IV'],
  [1, 'I']
];

@Pipe({
  name: 'romanNumeral'
})
export class RomanNumeralPipe implements PipeTransform {

  transform(value: number, ...args: any[]): any {
    return this.numberToRomanNumeral(value);
  }

  private numberToRomanNumeral(num: number) {
    if (num === 0)
      return '';

    for (var i = 0; i < romanMatrix.length; i++) {
      if (num >= romanMatrix[i][0]) {
        return romanMatrix[i][1] + this.numberToRomanNumeral(num - <number>romanMatrix[i][0]);
      }
    }
  }

}
