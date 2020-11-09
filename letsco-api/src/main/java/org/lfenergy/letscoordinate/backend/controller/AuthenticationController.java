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

package org.lfenergy.letscoordinate.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.client.KeycloakClient;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/letsco/api/v1")
@RequiredArgsConstructor
@Api(description = "Controller providing APIs to manage authentication")
public class AuthenticationController {

    private final UserService userService;
    private final KeycloakClient keycloakClient;

    @GetMapping("/auth/token")
    @ApiOperation(value = "Generate bearer access token by username and password")
    public ResponseEntity getToken(@RequestParam String username, @RequestParam String password) {
        return keycloakClient.getToken(username, password).fold(
                invalid -> ResponseEntity.status(invalid.getStatus()).body(invalid),
                valid -> ResponseEntity.ok("Bearer " + valid)
        );
    }

    @PostMapping("/auth/login")
    @ApiOperation(value = "Authenticate user using access token", hidden = true)
    @ApiImplicitParam(required = true, name = "Authorization", dataType = "string", paramType = "header")
    public ResponseEntity<Map> login() {
        User user = userService.getUserByUsername();
        Map result = new HashMap();
        result.put("username", user.getUsername());
        result.put("eicCode", user.getEicCode());
        return ResponseEntity.ok(result);
    }

}
