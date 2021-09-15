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

package org.lfenergy.letscoordinate.backend.controller;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Map<String, Object> resource_access = Stream.of(new Object[][]{{"TSO", "{}"},{"RSC", "{}"}})
                .collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        JwtAuthenticationToken auth = new JwtAuthenticationToken(new Jwt(
                "tokenValue", Instant.ofEpochMilli(Instant.now().toEpochMilli()-1000), Instant.ofEpochMilli(Instant.now().toEpochMilli()+1000000000),
                Stream.of(new Object[][]{ {"header1", "header1Value"} }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1])),
                Stream.of(new Object[][]{
                        {"sub", customUser.username()},
                        {"preferred_username", customUser.username()},
                        {"eicCode", "eicCode"},
                        {"resource_access", resource_access},
                        {"services", "SERVICE_A"}
                }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]))),
                Arrays.asList(new SimpleGrantedAuthority(customUser.authority())));
        context.setAuthentication(auth);
        return context;
    }
}