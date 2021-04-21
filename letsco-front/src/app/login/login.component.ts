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

import {AfterContentInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {environment} from "../../environments/environment";
import {AuthService} from "../core/services/auth.service";
import {EnvService} from "../core/services/env.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, AfterContentInit {

  redirectLoginUrl;

  @ViewChild('linkConnect', {static: true}) linkConnect: ElementRef<HTMLElement>;

  constructor(private auth: AuthService, private envService: EnvService) { }

  get openidAuthUrl() {
    return this.envService.openidUrl + this.envService.openidRealmsUrlPrefix + this.envService.openidRealms +
        this.envService.openidRealmsUrlSuffix + this.envService.openidAuthEndpoint;
  }

  redirectLogin(url) {
    window.location.href = url;
    return false;
  }

  ngOnInit() {
    console.log(location);
    this.redirectLoginUrl = this.openidAuthUrl +
        '?response_type=code' +
        '&scope=openid' +
        '&client_id=' + this.envService.openidClientId +
        '&redirect_uri=' + encodeURIComponent(location.origin + environment.appPrefix + (this.auth.redirectUrl ? this.auth.redirectUrl : '/login'));
  }

  ngAfterContentInit() {

    const el: HTMLElement = this.linkConnect.nativeElement;
    el.click();
  }
}