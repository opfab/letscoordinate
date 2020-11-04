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

import {AfterViewChecked, Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {ThemeService} from "./core/services/theme.service";

@Component({
    selector: 'app-root',
    template: `
        <router-outlet></router-outlet>`,
    styles: []
})
export class AppComponent implements AfterViewChecked {

    constructor(private route: ActivatedRoute,
                private themeService: ThemeService) {
    }

    ngAfterViewChecked() {
        const opfabTheme = localStorage.getItem('opfab_theme');
        if (opfabTheme) {
            this.themeService.initWithTheme(opfabTheme);
        } else {
            this.route.queryParams.subscribe(params => {
                const opfabTheme = params.opfab_theme;
                if (opfabTheme) {
                    this.themeService.initWithTheme(opfabTheme);
                }
            });
        }
    }


}