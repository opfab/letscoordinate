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

package org.lfenergy.letscoordinate.backend.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.config.OpfabConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.PropertiesDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.PayloadDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TextDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.ValidationMessageDto;
import org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum;
import org.lfenergy.letscoordinate.backend.model.opfab.ValidationData;
import org.lfenergy.letscoordinate.backend.util.DateUtil;
import org.lfenergy.letscoordinate.backend.util.JsonUtils;
import org.lfenergy.operatorfabric.cards.model.Card;
import org.lfenergy.operatorfabric.cards.model.RecipientEnum;
import org.lfenergy.operatorfabric.cards.model.SeverityEnum;
import org.lfenergy.operatorfabric.cards.model.TimeSpan;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.letscoordinate.backend.enums.ValidationSeverityEnum.*;
import static org.lfenergy.letscoordinate.backend.util.DateUtil.getParisZoneId;
import static org.lfenergy.letscoordinate.backend.util.StringUtil.*;
import static org.lfenergy.operatorfabric.cards.model.SeverityEnum.*;

public class OpfabPublisherComponentTest {

    private OpfabConfig opfabConfig;
    private OpfabPublisherComponent opfabPublisherComponent;
    private EventMessageDto eventMessageDto;
    private String source = "source";
    private String messageTypeName = "messageTypeName";
    private Instant timestamp = Instant.parse("2020-07-03T22:00:00.00Z");
    private Instant businessDayFrom = Instant.parse("2020-06-10T00:00:00Z");
    private Instant businessDayTo = Instant.parse("2020-06-17T00:00:00Z");
    private List<String> recipient = Arrays.asList("eic2", "eic3");
    private String sendingUser = "eic1";
    private PayloadDto payload;
    private BusinessDataIdentifierDto businessDataIdentifier;

    @BeforeEach
    public void before() {

        opfabConfig = new OpfabConfig("publisher", new OpfabConfig.OpfabUrls(), new HashMap<>(),
                new HashMap<>(), new HashMap<>());

        CoordinationConfig coordinationConfig = new CoordinationConfig();
        coordinationConfig.setTsos(Stream.of(new Object[][] {
                { "eic1", new CoordinationConfig.Tso( "eic1", "TSO1", "eicRsc1" ) },
                { "eic2", new CoordinationConfig.Tso( "eic2", "TSO2", "eicRsc2" ) },
                { "eic3", new CoordinationConfig.Tso( "eic3", "TSO3", "eicRsc3" ) }
        }).collect(Collectors.toMap(d -> (String) d[0], d -> (CoordinationConfig.Tso) d[1])));

        opfabPublisherComponent = new OpfabPublisherComponent(opfabConfig, coordinationConfig);

        TextDataDto text1 = new TextDataDto();
        text1.setName("nameText1");
        text1.setValue("valueText1");

        payload = PayloadDto.builder()
                .text(Arrays.asList(text1))
                .build();

        businessDataIdentifier = BusinessDataIdentifierDto.builder()
                .sendingUser(sendingUser)
                .recipients(recipient)
                .messageTypeName(messageTypeName)
                .businessDayFrom(businessDayFrom)
                .businessDayTo(businessDayTo)
                .build();

        eventMessageDto = EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .noun("noun")
                        .source(source)
                        .timestamp(timestamp)
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(businessDataIdentifier)
                                .build())
                        .build())
                .payload(payload)
                .build();
    }

    @Test
    public void setCardHeadersAndTags_OK() {
        Card card = new Card();
        Long id = 1L;
        opfabPublisherComponent.setCardHeadersAndTags(card, eventMessageDto, id);
        List<String> expectedTags = Arrays.asList(source, messageTypeName, source + "_" + messageTypeName).stream()
                .map(String::toLowerCase).collect(toList());
        String process = source + "_" + messageTypeName;
        assertAll(
                () -> assertEquals(expectedTags, card.getTags()),
                () -> assertEquals(process, card.getProcess()),
                () -> assertEquals(process + "_" + id, card.getProcessInstanceId()),
                () -> assertEquals(opfabConfig.getPublisher(), card.getPublisher()),
                () -> assertEquals("1", card.getProcessVersion()),
                () -> assertEquals("initial", card.getState())
        );
    }

    @Test
    public void setCardHeadersAndTags_MessageValidated_OK() throws IOException {
        EventMessageDto eventMessageDto =
                JsonUtils.jsonToObject("jsons/simpleMessageValidated_ok.json", EventMessageDto.class);
        Card card = new Card();
        Long id = 1L;
        opfabPublisherComponent.setCardHeadersAndTags(card, eventMessageDto, id);
        List<String> expectedTags = Arrays.asList(source, messageTypeName, source + "_" + messageTypeName).stream()
                .map(String::toLowerCase).collect(toList());
        expectedTags.add(source.toLowerCase() + "_" + messageTypeName.toLowerCase() + "_ok");
        assertEquals(expectedTags, card.getTags());

        eventMessageDto.getPayload().getValidation().get().setResult(WARNING);
        opfabPublisherComponent.setCardHeadersAndTags(card, eventMessageDto, id);
        expectedTags.set(3, source.toLowerCase() + "_" + messageTypeName.toLowerCase() + "_warning");
        assertEquals(expectedTags, card.getTags());

        eventMessageDto.getPayload().getValidation().get().setResult(ERROR);
        opfabPublisherComponent.setCardHeadersAndTags(card, eventMessageDto, id);
        expectedTags.set(3, source.toLowerCase() + "_" + messageTypeName.toLowerCase() + "_error");
        assertEquals(expectedTags, card.getTags());
    }

    @Test
    public void setOpfabCardDates_OK() {

        Card card = new Card();
        List<TimeSpan> expectedTimeSpans = Collections.singletonList(
                new TimeSpan().start(timestamp));
        opfabPublisherComponent.setOpfabCardDates(card, eventMessageDto);
        assertAll(
                () -> assertEquals(expectedTimeSpans, card.getTimeSpans()),
                () -> assertEquals(timestamp, card.getPublishDate()),
                () -> assertEquals(businessDayFrom, card.getStartDate()),
                () -> assertEquals(businessDayTo, card.getEndDate())
        );
    }

    @Test
    public void setCardRecipients_noRecipientNoSendingUser_OK() {

        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        businessDataIdentifier.setRecipients(null);
        businessDataIdentifier.setSendingUser(null);
        Card card = new Card();
        opfabPublisherComponent.setCardRecipients(card, eventMessageDto);
        assertAll(
                () -> assertEquals(RecipientEnum.GROUP, card.getRecipient().getType()),
                () -> assertEquals(source, card.getRecipient().getIdentity()),
                () -> assertEquals(Collections.EMPTY_LIST, card.getEntityRecipients())
        );
        businessDataIdentifier.setSendingUser(sendingUser);
        businessDataIdentifier.setRecipients(recipient);
    }

    @Test
    public void setCardRecipients_RecipientNoSendingUser_OK() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        businessDataIdentifier.setSendingUser(null);
        Card card = new Card();
        opfabPublisherComponent.setCardRecipients(card, eventMessageDto);
        assertAll(
                () -> assertEquals(RecipientEnum.GROUP, card.getRecipient().getType()),
                () -> assertEquals(source, card.getRecipient().getIdentity()),
                () -> assertEquals(recipient.stream().sorted().collect(Collectors.toList()),
                        card.getEntityRecipients().stream().sorted().collect(Collectors.toList()))
        );
        businessDataIdentifier.setSendingUser(sendingUser);
    }

    @Test
    public void setCardRecipients_SendingUserNoRecipient_OK() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        businessDataIdentifier.setRecipients(null);
        Card card = new Card();
        opfabPublisherComponent.setCardRecipients(card, eventMessageDto);
        assertAll(
                () -> assertEquals(RecipientEnum.GROUP, card.getRecipient().getType()),
                () -> assertEquals(source, card.getRecipient().getIdentity()),
                () -> assertEquals(Arrays.asList(sendingUser), card.getEntityRecipients())
        );
        businessDataIdentifier.setRecipients(recipient);
    }

    @Test
    public void setCardRecipients_SendingUserAndRecipient_OK() {
        Card card = new Card();
        opfabPublisherComponent.setCardRecipients(card, eventMessageDto);
        List<String> expectedEntityRecipients = new ArrayList<>();
        expectedEntityRecipients.add(sendingUser);
        expectedEntityRecipients.addAll(recipient);
        assertAll(
                () -> assertEquals(RecipientEnum.GROUP, card.getRecipient().getType()),
                () -> assertEquals(source, card.getRecipient().getIdentity()),
                () -> assertEquals(expectedEntityRecipients.stream().sorted().collect(Collectors.toList()),
                        card.getEntityRecipients().stream().sorted().collect(toList()))
        );
    }

    @Test
    public void specificCardTreatment_ProcessSuccess_NoProcessStepNoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(PROCESS_SUCCESS);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s process success", source),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), INFORMATION);
        specificCardTreatment_CommonAsserts_Data(card);
    }

    @Test
    public void specificCardTreatment_ProcessSuccess_NoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(PROCESS_SUCCESS);
        String processStep = "processStep";
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setProcessStep(processStep);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s process success - %s", source, processStep),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), INFORMATION);
        specificCardTreatment_CommonAsserts_Data(card);
    }

    @Test
    public void specificCardTreatment_ProcessSuccess_NoProcessStep_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(PROCESS_SUCCESS);
        String timeframe = "W";
        int timeframeNumber = 12;
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setTimeframe(timeframe);
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setTimeframeNumber(timeframeNumber);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s process success", source),
                String.format("%s%s %s-%s", timeframe, timeframeNumber,
                        DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), INFORMATION);
        specificCardTreatment_CommonAsserts_Data(card);
    }

    @Test
    public void specificCardTreatment_ProcessSuccess_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(PROCESS_SUCCESS);
        String processStep = "processStep";
        String timeframe = "W";
        int timeframeNumber = 12;
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setProcessStep(processStep);
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setTimeframe(timeframe);
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setTimeframeNumber(timeframeNumber);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s process success - %s", source, processStep),
                String.format("%s%s %s-%s", timeframe, timeframeNumber,
                        DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), INFORMATION);
        specificCardTreatment_CommonAsserts_Data(card);
    }

    @Test
    public void specificCardTreatment_ProcessFailed_NoProcessStepNoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(PROCESS_FAILED);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s process failed", source),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), ALARM);
        specificCardTreatment_CommonAsserts_Data(card);
    }

    @Test
    public void specificCardTreatment_MessageValidated_OK_NoProcessStepNoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        ValidationDto validation = ValidationDto.builder().result(OK).build();
        PayloadDto payload = PayloadDto.builder().validation(validation).build();
        eventMessageDto.setPayload(payload);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s message validated", source),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), COMPLIANT);
        assertAll(
                () -> assertNull(((ValidationData) card.getData()).getWarnings()),
                () -> assertNull(((ValidationData) card.getData()).getErrors())
        );
    }

    @Test
    public void specificCardTreatment_MessageValidated_WARNING_NoProcessStepNoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        addMessageValidatedData(WARNING);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s message validated", source),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), ACTION);
        assertAll(
                () -> assertEquals(3, ((ValidationData) card.getData()).getWarnings().size()),
                () -> assertEquals(0, ((ValidationData) card.getData()).getErrors().size())
        );
    }

    @Test
    public void specificCardTreatment_MessageValidated_ERROR_NoProcessStepNoTimeframeNoTimeframeNumber_OK() {

        Card card = new Card();
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        addMessageValidatedData(ERROR);
        opfabPublisherComponent.specificCardTreatment(card, eventMessageDto, 1L);
        specificCardTreatment_CommonAsserts(card, String.format("%s message validated", source),
                String.format("%s-%s", DateUtil.formatDate(businessDayFrom.atZone(getParisZoneId())),
                        DateUtil.formatDate(businessDayTo.atZone(getParisZoneId()))), ALARM);
        assertAll(
                () -> assertEquals(3, ((ValidationData) card.getData()).getWarnings().size()),
                () -> assertEquals(2, ((ValidationData) card.getData()).getErrors().size())
        );
    }

    private void addMessageValidatedData(ValidationSeverityEnum severity) {

        ValidationDto validation = new ValidationDto();
        validation.setResult(WARNING);

        List<ValidationMessageDto> validationMessages = new ArrayList<>(Arrays.asList(
                ValidationMessageDto.builder().severity(WARNING).build(),
                ValidationMessageDto.builder().severity(WARNING).build(),
                ValidationMessageDto.builder().severity(WARNING).build()
        ));

        if (severity == ERROR) {
            validation.setResult(ERROR);
            validationMessages.add(ValidationMessageDto.builder().severity(ERROR).build());
            validationMessages.add(ValidationMessageDto.builder().severity(ERROR).build());
        }

        validation.setValidationMessages(validationMessages);
        PayloadDto payload = PayloadDto.builder().validation(validation).build();
        eventMessageDto.setPayload(payload);
    }

    @Test
    public void publishOpfabCard_ProcessSuccess() throws Exception {
        eventMessageDto.getHeader().setNoun(PROCESS_SUCCESS);
        opfabPublisherComponent.publishOpfabCard(eventMessageDto, 1L);
    }

    @Test
    public void publishOpfabCard_ProcessFailed() throws Exception {
        eventMessageDto.getHeader().setNoun(PROCESS_FAILED);
        opfabPublisherComponent.publishOpfabCard(eventMessageDto, 1L);
    }

    @Test
    public void publishOpfabCard_ErrorMessageValidated() throws Exception {
        addMessageValidatedData(ERROR);
        eventMessageDto.getHeader().setNoun(MESSAGE_VALIDATED);
        opfabPublisherComponent.publishOpfabCard(eventMessageDto, 1L);
    }

    private void specificCardTreatment_CommonAsserts(Card card, String title, String summary, SeverityEnum severity) {
        assertAll(
                () -> assertEquals(title, card.getTitle().getParameters().get("title")),
                () -> assertEquals("cardFeed.title", card.getTitle().getKey()),
                () -> assertEquals("cardFeed.summary", card.getSummary().getKey()),
                () -> assertEquals(summary, card.getSummary().getParameters().get("summary")),
                () -> assertEquals(severity, card.getSeverity())
        );
    }

    private void specificCardTreatment_CommonAsserts_Data(Card card) {
        Map<String, Object> data = (Map<String, Object>) card.getData();
        assertAll(
                () -> assertEquals(eventMessageDto.getHeader().getNoun(), data.get("noun")),
                () -> assertEquals(eventMessageDto.getHeader().getSource(), data.get("source")),
                () -> assertEquals(eventMessageDto.getHeader().getTimestamp(), data.get("timestamp")),
                () -> assertEquals(eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier(),
                        data.get("businessDataIdentifier")),
                () -> assertEquals(eventMessageDto.getPayload(), data.get("payload"))
        );
    }

    @Test
    public void replacePlaceholders_containsPlaceholders() {
        Instant businessDayFrom = LocalDateTime.of(2020, 8, 25, 12, 0)
                .atZone(DateUtil.getParisZoneId()).toInstant();
        Instant businessDayTo = LocalDateTime.of(2020, 8, 30, 12, 0)
                .atZone(DateUtil.getParisZoneId()).toInstant();
        EventMessageDto eventMessageDto = EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(BusinessDataIdentifierDto.builder()
                                        .businessDayFrom(businessDayFrom)
                                        .businessDayTo(businessDayTo)
                                        .fileName("my filename")
                                        .messageTypeName("my messageTypeName").build())
                                .build())
                        .build())
                .build();
        String strWithPlaceholders = "Custom title - {{businessDayFrom::dateFormat(dd/MM/yyyy HH:mm)}}-" +
                "{{businessDayTo::dateFormat(dd/MM/yyyy HH:mm)}} - {{fileName}} - {{incorrectPlaceholder}} - " +
                "{{messageTypeName::incorrectMethod}}";
        String obtained = opfabPublisherComponent.replacePlaceholders(strWithPlaceholders, eventMessageDto);
        String expected = "Custom title - 25/08/2020 12:00-30/08/2020 12:00 - my filename -  - my messageTypeName";
        assertEquals(expected, obtained);
    }

    @Test
    public void replacePlaceholders_containsPlaceholders2() {
        Instant businessDayFrom =
                LocalDate.of(2020, 8, 25).atStartOfDay(ZoneOffset.UTC).toInstant();
        EventMessageDto eventMessageDto = EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(BusinessDataIdentifierDto.builder()
                                        .businessDayFrom(businessDayFrom).build())
                                .build())
                        .build())
                .build();
        String str = "Custom title - ${businessDayFrom}";
        opfabPublisherComponent.replacePlaceholders(str, eventMessageDto);
    }

    @Test
    public void generateCardData_sendingUserNull() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        String sendingUserOld = businessDataIdentifier.getSendingUser().orElse(null);
        businessDataIdentifier.setSendingUser(null);
        Map<String, Object> dataObtained = opfabPublisherComponent.generateCardData(eventMessageDto, 1L);
        businessDataIdentifier.setSendingUser(sendingUserOld);
    }

    @Test
    public void generateCardData_sendingUserNotTsoEicCode() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        String sendingUserOld = businessDataIdentifier.getSendingUser().orElse(null);
        businessDataIdentifier.setSendingUser("eicIncorrect");
        Map<String, Object> dataObtained = opfabPublisherComponent.generateCardData(eventMessageDto, 1L);
        businessDataIdentifier.setSendingUser(sendingUserOld);
    }

    @Test
    public void generateCardData_recipientNull() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        List<String> recipientOld = businessDataIdentifier.getRecipients().orElse(null);
        businessDataIdentifier.setRecipients(null);
        Map<String, Object> dataObtained = opfabPublisherComponent.generateCardData(eventMessageDto, 1L);
        businessDataIdentifier.setRecipients(recipientOld);
    }

    @Test
    public void generateCardData_recipientNoneValidTsoEicCode() {
        BusinessDataIdentifierDto businessDataIdentifier =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier();
        List<String> recipientOld = businessDataIdentifier.getRecipients().orElse(null);
        businessDataIdentifier.setRecipients(Arrays.asList("eicIncorrect"));
        Map<String, Object> dataObtained = opfabPublisherComponent.generateCardData(eventMessageDto, 1L);
        businessDataIdentifier.setRecipients(recipientOld);
    }

    @Test
    public void generateCardData() {
        Map<String, Object> dataObtained = opfabPublisherComponent.generateCardData(eventMessageDto, 1L);
        assertAll(
                () -> assertEquals(1L, dataObtained.get("cardId")),
                () -> assertEquals("noun", dataObtained.get("noun")),
                () -> assertEquals(source, dataObtained.get("source")),
                () -> assertEquals(timestamp, dataObtained.get("timestamp")),
                () -> assertEquals(businessDataIdentifier, dataObtained.get("businessDataIdentifier")),
                () -> assertEquals("TSO1", dataObtained.get("sendingUser")),
                () -> assertEquals(Arrays.asList("TSO2", "TSO3"), dataObtained.get("recipient")),
                () -> assertEquals(payload, dataObtained.get("payload"))
        );
    }

    @Test
    public void generatePlaceholderValue_dateFormat_withZoneId() {
        String placeholder = "{{businessDayFrom::dateFormat(dd/MM/yyyy HH:mm::Europe/Paris)}}";
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map<String, Object> bdiMap = Map.of("businessDayFrom", "2019-11-10T23:00:00Z");
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "11/11/2019 00:00");
        assertEquals(expectedResult, obtainedResult);
    }

    @Test
    public void generatePlaceholderValue_dateFormat_noZoneId() {
        String placeholder = "{{businessDayFrom::dateFormat(dd/MM/yyyy HH:mm)}}";
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map<String, Object> bdiMap = Map.of("businessDayFrom", "2019-11-10T23:00:00Z");
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "11/11/2019 00:00");
        assertEquals(expectedResult, obtainedResult);
    }

    @Test
    public void generatePlaceholderValue_dateFormat_dateAbsent() {
        String placeholder = "{{businessDayFrom::dateFormat(dd/MM/yyyy HH:mm)}}";
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map<String, Object> bdiMap = new HashMap<>();
        bdiMap.put("businessDayFrom", null);
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "");
        assertEquals(expectedResult, obtainedResult);
    }

    @Test
    public void generatePlaceholderValue_validationStatus() {
        String placeholder = "{{validationStatus}}";
        EventMessageDto eventMessageDto = EventMessageDto.builder().payload(PayloadDto.builder()
                .validation(ValidationDto.builder().result(ERROR).build()).build()).build();
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, new HashMap<>(), eventMessageDto);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "Negative");
        assertEquals(expectedResult, obtainedResult);
    }

    @Test
    public void generatePlaceholderValue_other() {
        String placeholder = "{{anyPlaceholder}}";
        Map<String, Object> bdiMap = Map.of("anyPlaceholder", "anyPlaceholderValue");
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry(placeholder, null);
        Map.Entry<String, String> obtainedResult =
                opfabPublisherComponent.generatePlaceholderValue(entry, bdiMap, null);
        Map.Entry<String, String> expectedResult = new AbstractMap.SimpleEntry<>(placeholder, "anyPlaceholderValue");
        assertEquals(expectedResult, obtainedResult);
    }
}
