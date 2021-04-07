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
import org.lfenergy.letscoordinate.backend.enums.BasicGenericNounEnum;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class LetscoPropertiesTest {

    LetscoProperties letscoProperties;

    @BeforeEach
    public void beforeEach() {
        letscoProperties = new LetscoProperties();
        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        inputFile.setGenericNouns(Map.of(
                BasicGenericNounEnum.PROCESS_SUCCESSFUL, List.of("otherPS_1", "otherPS_2"),
                BasicGenericNounEnum.PROCESS_FAILED, List.of("otherPF_1")));
        letscoProperties.setInputFile(inputFile);
    }

    @Test
    public void allGenericNouns() {
        assertEquals(
                List.of("ProcessSuccessful", "ProcessFailed", "otherPS_1", "otherPS_2", "otherPF_1", "ProcessAction",
                        "ProcessInformation", "DfgMessageValidated").stream().sorted().collect(toList()),
                letscoProperties.getInputFile().allGenericNouns().stream().sorted().collect(toList()));
    }
}
