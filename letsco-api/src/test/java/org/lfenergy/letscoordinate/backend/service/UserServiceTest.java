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

package org.lfenergy.letscoordinate.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    UserService userService;
    @Mock
    UserRepository userRepository;

    @BeforeEach
    public void before() {
        userService = new UserService(userRepository);

        SecurityContextHolder.getContext().setAuthentication(ApplicationContextUtil.createTestAuthentication());
    }

    @Test
    public void getUserByUsername_shouldReturnFoundUser() {
        User user = User.builder()
                .id(1L)
                .username("user.test")
                .eicCode(null)
                .userServices(Arrays.asList(
                        org.lfenergy.letscoordinate.backend.model.UserService.builder()
                                .id(1L)
                                .serviceCode("SERVICE_A")
                                .user(User.builder().id(1L).build())
                                .build(),
                        org.lfenergy.letscoordinate.backend.model.UserService.builder()
                                .id(2L)
                                .serviceCode("SERVICE_B")
                                .user(User.builder().id(1L).build())
                                .build())
                )
                .build();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        User returndUser = userService.getUserByUsername();
        assertAll(
                () -> assertNotNull(returndUser),
                () -> assertSame(user, returndUser)
        );
    }

    @Test
    public void getUserByUsername_shouldNewCreatedUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).then(i -> i.getArgument(0, User.class));

        User returndUser = userService.getUserByUsername();
        assertAll(
                () -> assertNotNull(returndUser),
                () -> assertEquals("user.test", returndUser.getUsername()),
                () -> assertEquals(2, returndUser.getUserServices().size())
        );
    }

}
