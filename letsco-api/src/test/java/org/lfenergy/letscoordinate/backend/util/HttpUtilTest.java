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

package org.lfenergy.letscoordinate.backend.util;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiDto;
import org.lfenergy.letscoordinate.backend.dto.reporting.RscKpiTypedDataDto;
import org.lfenergy.letscoordinate.backend.enums.KpiDataTypeEnum;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)
public class HttpUtilTest {


    @BeforeEach
    public void before() {
    }

    @Test
    public void post_withEmptyUrl() {
        String url = "";
        String data = "J'aime la tourte!";
        String response = HttpUtil.post(url, data);
        assertEquals("", response);

    }

    @Test
    public void post_withUnknownUrl() {
        String url = "http://unknown_host/";
        String data = "J'aime la tourte!";
        String response = HttpUtil.post(url, data);
        assertNotNull(response);
        assertEquals("", response);
    }

    @Test
    public void post_withUrlAndStringData() {
        String url = "http://localhost:80/";
        String data = "J'aime la tourte!";
        String response = HttpUtil.post(url, data);
        assertNotNull(response);
        assertEquals("", response);
    }

    @Test
    public void post_withUrlAndObjectData() {
        String url = "http://localhost:80/";
        RscKpiTypedDataDto data = RscKpiTypedDataDto.builder()
                .type(KpiDataTypeEnum.GP)
                .rscKpis(Arrays.asList(RscKpiDto.builder()
                        .name("GP1")
                        .joinGraph(true)
                        .dataMap(new HashMap<>())
                        .build()))
                .build();
        String response = HttpUtil.post(url, data);
        assertNotNull(response);
        assertEquals("", response);
    }

}
