/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.client;

import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.config.KeycloakConfig;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class KeycloakClientTest {

    private KeycloakConfig keycloakConfig;
    private KeycloakClient keycloakClient;

    @BeforeEach
    public void before() {
        KeycloakConfig.Server server = new KeycloakConfig.Server();
        server.setClientId("letsco_id");
        server.setClientSecret("client_secret");
        server.setUrl("url_test");
        server.setRealm("realm_test");
        keycloakConfig = new KeycloakConfig();
        keycloakConfig.setServer(server);
        keycloakClient = new KeycloakClient(keycloakConfig);
    }

    @Test
    public void getToken_shouldReturnInvalid() {
        Validation<ResponseErrorDto, String> validation = keycloakClient.getToken("", "");
        assertNotNull(validation);
        assertTrue(validation.isInvalid());
    }

}
