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

package org.lfenergy.letscoordinate.backend.client;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.lfenergy.letscoordinate.backend.config.KeycloakConfig;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient {

    private final KeycloakConfig keycloakConfig;

    public Validation<ResponseErrorDto, String> getToken(String username, String password) {
        try {
            Keycloak instance = Keycloak.getInstance(
                    keycloakConfig.getServer().getUrl(),
                    keycloakConfig.getServer().getRealm(),
                    username,
                    password,
                    keycloakConfig.getServer().getClientId(),
                    keycloakConfig.getServer().getClientSecret());
            TokenManager tokenmanager = instance.tokenManager();
            return Validation.valid(tokenmanager.getAccessTokenString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Validation.invalid(ResponseErrorDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .code("TOKEN_GENERATION_ERROR")
                    .message("Error while generating access token for user: " + username)
                    .detail(e.getMessage())
                    .build());
        }
    }

}
