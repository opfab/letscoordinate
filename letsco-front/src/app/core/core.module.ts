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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { RomanNumeralPipe } from './pipes/roman-numeral.pipe';
import { AlphabeticNumeralPipe } from './pipes/alphabetic-numeral.pipe';
import { KpiDataTypeFullNamePipe } from './pipes/kpi-data-type-full-name.pipe';



@NgModule({
    declarations: [RomanNumeralPipe, AlphabeticNumeralPipe, KpiDataTypeFullNamePipe],
    exports: [
        RomanNumeralPipe,
        AlphabeticNumeralPipe,
        KpiDataTypeFullNamePipe
    ],
    imports: [
        CommonModule,
        HttpClientModule
    ]
})
export class CoreModule { }
