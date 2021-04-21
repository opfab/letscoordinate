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

package org.lfenergy.letscoordinate.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.model.User;
import org.lfenergy.letscoordinate.backend.repository.UserRepository;
import org.lfenergy.letscoordinate.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User getUserByUsername() {
        String username = SecurityUtil.getUsernameFromToken();

        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()) {
            return user.get();
        }
        log.debug("User \"{}\" not found in letsco database, it will be created.", username);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setUserServices(SecurityUtil.getServicesFromToken().stream()
                .map(srv -> org.lfenergy.letscoordinate.backend.model.UserService.builder()
                        .serviceCode(srv)
                        .user(newUser)
                        .build())
                .collect(Collectors.toList()));

        return userRepository.save(newUser);
    }

}
