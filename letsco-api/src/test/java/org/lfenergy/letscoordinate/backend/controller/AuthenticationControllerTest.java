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

import io.vavr.control.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.client.KeycloakClient;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorDto;
import org.lfenergy.letscoordinate.backend.dto.ResponseErrorMessageDto;
import org.lfenergy.letscoordinate.backend.enums.ResponseErrorSeverityEnum;
import org.lfenergy.letscoordinate.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    KeycloakClient keycloakClient;
    @MockBean
    org.lfenergy.letscoordinate.backend.service.UserService userService;

    @Test
    public void getToken_withValidUsernamePassword() throws Exception {
        when(keycloakClient.getToken(anyString(), anyString())).thenReturn(Validation.valid("azerty"));
        mockMvc.perform(get("/letsco/api/v1/auth/token")
                .param("username", "user.test")
                .param("password", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Bearer azerty"));
    }

    @Test
    public void getToken_withUnknownUsernamePassword() throws Exception {
        when(keycloakClient.getToken(anyString(), anyString())).then(i ->
                Validation.invalid(ResponseErrorDto.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code("TOKEN_GENERATION_ERROR")
                        .messages(Collections.singletonList(ResponseErrorMessageDto.builder()
                                .severity(ResponseErrorSeverityEnum.ERROR)
                                .message("Error while generating access token for user: " + i.getArgument(0, String.class))
                                .detail("Error while generating access token for user: " + i.getArgument(0, String.class))
                                .build()))
                        .build())
        );
        mockMvc.perform(get("/letsco/api/v1/auth/token")
                .param("username", "UNKNOWN")
                .param("password", "UNKNOWN"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("TOKEN_GENERATION_ERROR"))
                .andExpect(jsonPath("$.messages", hasSize(1)));
    }

    @Test
    @WithMockCustomUser
    public void login_shouldReturn200() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("user.test")
                .eicCode("eic_code")
                .build();
        when(userService.getUserByUsername()).thenReturn(user);
        mockMvc.perform(post("/letsco/api/v1/auth/login")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.eicCode").value(user.getEicCode()));
    }

}
