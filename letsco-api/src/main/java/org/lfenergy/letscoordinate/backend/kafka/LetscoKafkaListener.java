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

package org.lfenergy.letscoordinate.backend.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.enums.MessageTypeEnum;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.HttpUtil;
import org.lfenergy.letscoordinate.backend.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.lfenergy.letscoordinate.backend.util.StringUtil.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetscoKafkaListener {

    @Value("${third-app.url}")
    private String thirdAppUrl;

    private final JsonDataProcessor jsonDataProcessor;

    private final EventMessageRepository eventMessageRepository;
    private final OpfabPublisherComponent opfabPublisherComponent;

    @KafkaListener(topicPattern = "letsco_eventmessage_.*")
    public void handleLetscoOpcMergeResult(@Payload String data,
                                           @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                           @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) throws Exception {
        log.info("Data receiced from topic \"{}\" (kafka_receivedTimestamp = {}, kafka_receivedPartitionId = {})", topic,
                DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(ts)), partition);
        log.debug("Received data:\n {}", data);
        EventMessageDto eventMessageDto = jsonDataProcessor.inputStreamToPojo(new ByteArrayInputStream(data.getBytes()));

        // LC-254 (Change Request) MR2: Remove a second from the end of the business period (businessDayTo) to avoid
        // displaying the card in the next month
        BusinessDataIdentifierDto businessDataIdentifierDto = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        businessDataIdentifierDto.setBusinessDayTo((businessDataIdentifierDto.getBusinessDayTo().orElse(
                businessDataIdentifierDto.getBusinessDayFrom().plus(Duration.ofHours(24)))).minus(Duration.ofSeconds(1)));

        String messageTypeNameId = StringUtil.toLowercaseIdentifier(eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName());
        if (!isGenericMessageTypeName(messageTypeNameId)) {
            String url = String.format("%s/api/json", thirdAppUrl);
            HttpUtil.post(url, data);
            return;
        }

        log.info("Received data type: \"{}\"", eventMessageDto.getHeader().getNoun());
        EventMessage eventMessage = eventMessageRepository.save(EventMessageMapper.fromDto(eventMessageDto));
        log.info("New \"{}\" data successfully saved! (id={})", eventMessage.getNoun(), eventMessage.getId());
        log.debug("Saved data >>> {}", eventMessage.toString());

        opfabPublisherComponent.publishOpfabCard(eventMessageDto, eventMessage.getId());
    }

    private boolean isGenericMessageTypeName(String messageTypeNameId) {
        return Arrays.stream(MessageTypeEnum.values()).map(MessageTypeEnum::getId).collect(Collectors.toList())
                .contains(messageTypeNameId);
    }
}
