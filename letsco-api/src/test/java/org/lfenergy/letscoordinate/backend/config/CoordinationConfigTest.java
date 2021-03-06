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
import org.lfenergy.letscoordinate.backend.util.ApplicationContextUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class CoordinationConfigTest {

    CoordinationConfig coordinationConfig;

    @BeforeEach
    public void before() {
        coordinationConfig = ApplicationContextUtil.initCoordinationConfig();
    }

    @Test
    public void getServiceCodes() {
        Set<String> serviceCodes = coordinationConfig.getServiceCodes();
        assertNotNull(serviceCodes);
        assertFalse(serviceCodes.isEmpty());
        assertEquals(2, serviceCodes.size());
    }

    @Test
    public void getTsoByEicCode_knownEicCode() {
        CoordinationConfig.Tso tso = coordinationConfig.getTsoByEicCode("10XFR-RTE------Q");
        assertNotNull(tso);
        assertEquals("10XFR-RTE------Q", tso.getEicCode());
    }

    @Test
    public void getTsoByEicCode_unknownEicCode() {
        CoordinationConfig.Tso tso = coordinationConfig.getTsoByEicCode("UNKNOWN_EIC_CODE");
        assertNull(tso);
    }

    @Test
    public void getServicesByCodes_knownCode() {
        List<CoordinationConfig.Service> services = coordinationConfig.getServicesByCodes(Arrays.asList("SERVICE_A", "SERVICE_B"));
        assertNotNull(services);
        assertFalse(services.isEmpty());
        assertEquals(2, services.size());
    }

    @Test
    public void getServicesByCodes_unknownCode() {
        List<CoordinationConfig.Service> services = coordinationConfig.getServicesByCodes(Arrays.asList("TOTO", "TATA"));
        assertTrue(services.isEmpty());
    }

    @Test
    public void getServicesByCodes_nullInput() {
        List<CoordinationConfig.Service> services = coordinationConfig.getServicesByCodes(null);
        assertNull(services);
    }

}
