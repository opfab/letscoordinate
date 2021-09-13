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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationEntityRaResponseEnum;
import org.lfenergy.letscoordinate.backend.model.Coordination;
import org.lfenergy.letscoordinate.backend.model.CoordinationRa;
import org.lfenergy.letscoordinate.backend.model.CoordinationRaAnswer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class OpfabUtilTest {

    private EventMessageDto initEventMessageDto() {
        EventMessageDto eventMessageDto = new EventMessageDto();
        eventMessageDto.getHeader().setSource("Source");
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setMessageTypeName("MessageTypeName");
        eventMessageDto.getHeader().setTimestamp(Instant.parse("2021-05-31T05:13:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(Instant.parse("2021-05-31T00:00:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayTo(Instant.parse("2021-05-31T23:59:59Z"));

        return eventMessageDto;
    }

    private Coordination initCoordination(CoordinationEntityRaResponseEnum answer1, CoordinationEntityRaResponseEnum answer2) {
        Coordination coordination = new Coordination();
        List<CoordinationRaAnswer> answers = new ArrayList<>();
        if (answer1 != null)
            answers.add(CoordinationRaAnswer.builder()
                    .eicCode("10XFR-RTE------Q")
                    .answer(answer1)
                    .build());
        if (answer2 != null)
            answers.add(CoordinationRaAnswer.builder()
                .eicCode("10X1001A1001A345")
                .answer(answer2)
                .build());
        coordination.setCoordinationRas(Arrays.asList(
                CoordinationRa.builder()
                        .event("Event A")
                        .constraintt("Constraint A")
                        .remedialAction("RemedialActions A")
                        .coordinationRaAnswers(answers)
                        .build()
        ));
        return coordination;
    }

    @Test
    public void generateProcessKey_toLowerCaseIdentifier_true() {
        String processKey = OpfabUtil.generateProcessKey(initEventMessageDto(), true);
        assertAll(
                () -> assertNotNull(processKey),
                () -> assertEquals("source_messagetypename", processKey)
        );
    }

}
