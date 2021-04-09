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

package org.lfenergy.letscoordinate.dataprovider;

import org.junit.jupiter.api.Test;
import org.lfenergy.letscoordinate.dataprovider.config.LetscoKafkaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LetscoDataProviderApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void main() {
        LetscoDataProviderApplication.main(new String[] {});
    }

    @Test
    public void CoordinatorConfig() {
        String[] beansNames = applicationContext.getBeanDefinitionNames();
        assertTrue(Arrays.stream(beansNames).anyMatch("swaggerConfig"::equals));
        LetscoKafkaProperties letscoKafkaProperties = applicationContext.getBean(LetscoKafkaProperties.class);
        assertNotNull(letscoKafkaProperties.getBootstrapServers());
        assertNotNull(letscoKafkaProperties.getTopicNamePrefix());
    }
}
