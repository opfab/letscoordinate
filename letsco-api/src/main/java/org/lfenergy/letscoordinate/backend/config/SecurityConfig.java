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

package org.lfenergy.letscoordinate.backend.config;

import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final LetscoProperties letscoProperties;

    /**
     * Secure the endpoints of the application
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().ignoringAntMatchers("/letsco/api/v1/coordination") //NOSONAR
                .and()
                .cors()
                .and()
                .authorizeRequests() //NOSONAR
                .mvcMatchers("/letsco/api/v1/auth/token").permitAll()
                .mvcMatchers("/letsco/api/v1/coordination").permitAll()
                .mvcMatchers("/letsco/api/v1/**").hasAnyRole("TSO", "RSC")
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(grantedAuthoritiesExtractor());
    }

    /**
     * Create the roles from the JWT token
     *
     * @return
     */
    Converter<Jwt, AbstractAuthenticationToken> grantedAuthoritiesExtractor() {
        return new GrantedAuthoritiesExtractor(letscoProperties.getSecurity().getClientId());
    }

    /**
     * Create roles from the values available in the JWT token
     *
     */
    static class GrantedAuthoritiesExtractor extends JwtAuthenticationConverter {

        String clientId;

        GrantedAuthoritiesExtractor(String clientId) {
            this.clientId = clientId;
        }

        protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

            Map<String, Map<String, Collection<String>>> resource_access = (Map<String, Map<String, Collection<String>>>) jwt.getClaims().get("resource_access");

            Collection<String> authorities = resource_access.get(clientId).get("roles").stream()
                    .map(role -> Constants.ROLE_PREFIX + role.toUpperCase())
                    .collect(Collectors.toList());

            return authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}

