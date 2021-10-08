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

import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EnvService} from './env.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    redirectUrl = '/kpi-report-config';

    constructor(private http: HttpClient, private envService: EnvService) { }

    openidTokenUrl = this.envService.openidUrl + this.envService.openidRealmsUrlPrefix + this.envService.openidRealms + this.envService.openidRealmsUrlSuffix + this.envService.openidTokenEndpoint;

    retrieveTokenData(code, redirectUri): Observable<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type':  'application/x-www-form-urlencoded',
                'Authorization': 'Basic ' + this.envService.openidBasicAuth
            })};

        const body = new HttpParams()
            .set('grant_type', 'authorization_code')
            .set('code', code)
            .set('redirect_uri', redirectUri);

        return this.http.post(this.openidTokenUrl, body, httpOptions);
    }

    logUser(datas) {
        localStorage.setItem('token', datas.access_token);
        localStorage.setItem('refresh_token', datas.refresh_token);
        localStorage.setItem('expires', (Date.now() + parseInt(datas.expires_in, 10) * 1000).toString());
    }

    get tokenHeader() {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type':  'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            })
        };
        return httpOptions;
    }

    logout() {
        localStorage.removeItem('token');
    }

    public get loggedIn(): boolean {
        return (localStorage.getItem('token') !== null) && Date.now() < parseInt(localStorage.getItem('expires'), 10);
    }

}