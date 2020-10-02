/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageWrapperDto;
import org.lfenergy.letscoordinate.backend.exception.InvalidInputFileException;
import org.lfenergy.letscoordinate.backend.service.EventMessageService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JsonDataProcessor implements DataProcessor {

    private final ObjectMapper objectMapper;
    private final EventMessageService eventMessageService;

    public EventMessageDto inputStreamToPojo(InputStream inputStream) throws IOException, InvalidInputFileException {
        EventMessageDto eventMessageDto = Optional.ofNullable(objectMapper.readValue(inputStream, EventMessageWrapperDto.class))
                .map(EventMessageWrapperDto::getEventMessage)
                .orElse(null);
        eventMessageService.checkEicCodes(eventMessageDto);
        return eventMessageDto;
    }
}
