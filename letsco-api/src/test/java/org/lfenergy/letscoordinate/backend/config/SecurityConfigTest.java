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

package org.lfenergy.letscoordinate.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class SecurityConfigTest {

    SecurityConfig.GrantedAuthoritiesExtractor grantedAuthoritiesExtractor;

    @BeforeEach
    void before() {
        grantedAuthoritiesExtractor = new SecurityConfig.GrantedAuthoritiesExtractor("clientId");
    }

    @Test
    void extractAuthorities_rolesExist() {
        Map<String, Collection<String>> clientClaims = new HashMap<>();
        clientClaims.put("roles", List.of("TSO", "RSC"));
        Map<String, Map<String, Collection<String>>> resourceAccess = new HashMap<>();
        resourceAccess.put("clientId", clientClaims);
        Jwt jwt = Jwt.withTokenValue("letsco")
                .header("typ", "JWT")
                .claim("sub", "user.test")
                .claim("resource_access", resourceAccess)
                .build();
        Collection<GrantedAuthority> authorities = grantedAuthoritiesExtractor.extractAuthorities(jwt);
        assertAll(
                () -> assertFalse(authorities.isEmpty()),
                () -> assertEquals(2, authorities.size())
        );
    }

    @Test
    void extractAuthorities_rolesNotExist() {
        Map<String, Collection<String>> clientClaims = new HashMap<>();
        Map<String, Map<String, Collection<String>>> resourceAccess = new HashMap<>();
        resourceAccess.put("clientId", clientClaims);
        Jwt jwt = Jwt.withTokenValue("letsco")
                .header("typ", "JWT")
                .claim("sub", "user.test")
                .claim("resource_access", resourceAccess)
                .build();
        Collection<GrantedAuthority> authorities = grantedAuthoritiesExtractor.extractAuthorities(jwt);
        assertAll(
                () -> assertTrue(authorities.isEmpty())
        );
    }

    @Test
    void extractAuthorities_clientIdNotFound() {
        Map<String, Map<String, Collection<String>>> resourceAccess = new HashMap<>();
        Jwt jwt = Jwt.withTokenValue("letsco")
                .header("typ", "JWT")
                .claim("sub", "user.test")
                .claim("resource_access", resourceAccess)
                .build();
        Collection<GrantedAuthority> authorities = grantedAuthoritiesExtractor.extractAuthorities(jwt);
        assertAll(
                () -> assertTrue(authorities.isEmpty())
        );
    }

}
