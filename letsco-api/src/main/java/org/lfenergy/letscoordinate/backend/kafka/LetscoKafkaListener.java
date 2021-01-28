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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.letscoordinate.backend.component.OpfabPublisherComponent;
import org.lfenergy.letscoordinate.backend.config.LetscoProperties;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.enums.ChangeJsonDataFromWhichEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.enums.ValidationTypeEnum;
import org.lfenergy.letscoordinate.backend.exception.IgnoreProcessException;
import org.lfenergy.letscoordinate.backend.exception.PositiveTechnicalQualityCheckException;
import org.lfenergy.letscoordinate.backend.mapper.EventMessageMapper;
import org.lfenergy.letscoordinate.backend.model.EventMessage;
import org.lfenergy.letscoordinate.backend.processor.JsonDataProcessor;
import org.lfenergy.letscoordinate.backend.repository.EventMessageRepository;
import org.lfenergy.letscoordinate.backend.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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
    private final LetscoProperties letscoProperties;

    @KafkaListener(topicPattern = "#{@kafkaTopicPattern}")
    public void handleLetscoOpcMergeResult(@Payload String data,
                                           @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                           @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) throws Exception {
        log.info("Data receiced from topic \"{}\" (kafka_receivedTimestamp = {}, kafka_receivedPartitionId = {})", topic,
                DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(ts)), partition);
        log.debug("Received data:\n {}", data);
        EventMessageDto eventMessageDto = jsonDataProcessor.inputStreamToPojo(new ByteArrayInputStream(data.getBytes()));

        String noun = eventMessageDto.getHeader().getNoun();
        if (!isGenericNoun(noun)) {
            String url = String.format("%s/api/json", thirdAppUrl);
            HttpUtil.post(url, data);
            return;
        }

        try {
            verifyData(eventMessageDto);

            log.info("Received data type: \"{}\"", eventMessageDto.getHeader().getNoun());
            EventMessage eventMessage = eventMessageRepository.save(EventMessageMapper.fromDto(eventMessageDto));
            log.info("New \"{}\" data successfully saved! (id={})", eventMessage.getNoun(), eventMessage.getId());
            log.debug("Saved data >>> {}", eventMessage.toString());

            opfabPublisherComponent.publishOpfabCard(eventMessageDto, eventMessage.getId());

        } catch (IgnoreProcessException | PositiveTechnicalQualityCheckException e) {
            log.info(e.getMessage());
        }
    }

    void verifyData(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        String process = source + "_" + messageTypeName;
        BusinessDataIdentifierDto bdi = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        ignoreProcessIfNeeded(process);
        ignorePositiveTechnicalQualityCheck(eventMessageDto);
        processIfBusinessDayFromOptional(bdi, eventMessageDto.getHeader().getTimestamp());
        changeMessageTypeNameIfNeeded(messageTypeName, bdi);
        changeSourceIfNeeded(source, eventMessageDto);
    }

    void ignoreProcessIfNeeded(String process) {
        letscoProperties.getInputFile().getValidation().getIgnoreProcesses().ifPresent(processesToIgnore -> {
            if (processesToIgnore.contains(process)) {
                throw new IgnoreProcessException("Json message ignored. Process: " + process);
            }
        });
    }

    void ignorePositiveTechnicalQualityCheck(EventMessageDto eventMessageDto) {
        if (MESSAGE_VALIDATED.equals(eventMessageDto.getHeader().getNoun())) {
            ValidationDto validationDto = eventMessageDto.getPayload().getValidation();
            if (validationDto.getResult() == ValidationSeverityEnum.OK &&
                    validationDto.getValidationType() == ValidationTypeEnum.TECHNICAL) {
                throw new PositiveTechnicalQualityCheckException("Positive technical quality check => no need to process it");
            }
        }
    }

    void processIfBusinessDayFromOptional(BusinessDataIdentifierDto bdi, Instant timestamp) {
        if (letscoProperties.getInputFile().getValidation().isBusinessDayFromOptional()) {
            if (Objects.isNull(bdi.getBusinessDayFrom())) {
                bdi.setBusinessDayFrom(timestamp);
            }
        }
    }

    void changeMessageTypeNameIfNeeded(String messageTypeName, BusinessDataIdentifierDto bdi) {
        letscoProperties.getInputFile().getValidation().getChangeMessageTypeName().ifPresent(m -> {
            if (m.containsKey(messageTypeName)) {
                bdi.setMessageTypeName(m.get(messageTypeName));
            }
        });
    }

    void changeSourceIfNeeded(String source, EventMessageDto eventMessageDto) {
        letscoProperties.getInputFile().getValidation().getChangeSource().ifPresent(m -> {
            if (m.containsKey(source)) {
                if (m.get(source).getFromWhichLevel() == ChangeJsonDataFromWhichEnum.HEADER) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> headerMap = mapper.convertValue(eventMessageDto.getHeader(), Map.class);
                    String newSource = headerMap.get(m.get(source).getChangingField()).toString();
                    eventMessageDto.getHeader().setSource(newSource);
                } else if (m.get(source).getFromWhichLevel() == ChangeJsonDataFromWhichEnum.BUSINESS_DATA_IDENTIFIER) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> bdiMap = mapper.convertValue(
                            eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier(), Map.class);
                    String newSource = bdiMap.get(m.get(source).getChangingField()).toString();
                    eventMessageDto.getHeader().setSource(newSource);
                }
            }
        });
    }

    private boolean isGenericNoun(String noun) {
        return Arrays.asList(PROCESS_SUCCESS, PROCESS_FAILED, PROCESS_ACTION, PROCESS_INFORMATION,
                MESSAGE_VALIDATED).contains(noun);
    }
}
