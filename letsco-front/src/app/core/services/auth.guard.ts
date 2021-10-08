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
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';
import {AuthService} from './auth.service';
import {flatMap, map} from 'rxjs/operators';
import {environment} from "../../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {

    constructor(private auth: AuthService, private router: Router) {
    }

    canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

        const splittedUrl = state.url.split('?');
        this.auth.redirectUrl = splittedUrl[0];
        if (splittedUrl.length > 1) {
            const params = splittedUrl[1].split('&');
            if (params !== undefined && params.filter(p => p.includes('opfab_theme')).length > 0) {
                localStorage.setItem('opfab_theme', params.filter(p => p.includes('opfab_theme'))[0]
                    .replace('opfab_theme=', ''));
            }
        }

        if (this.auth.loggedIn) {
            return true;
        }

        const redirectUri = location.origin + environment.appPrefix + '/' + next.routeConfig.path;
        const code = next.queryParams.code;

        if (code === undefined) {
            this.router.navigate(['/login']);
            return false;
        }

        const tokenDataObs: Observable<boolean> = this.auth.retrieveTokenData(code, redirectUri)
            .pipe(
                flatMap(tokenData => {
                    this.auth.logUser(tokenData);
                    return of(true);
                })
            );

        return tokenDataObs;
    }
}