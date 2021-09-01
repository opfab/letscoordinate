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

package org.lfenergy.letscoordinate.backend.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.CoordinationCommentDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.PropertiesDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.lfenergy.letscoordinate.backend.model.*;
import org.lfenergy.letscoordinate.backend.util.StringUtil;

import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMessageMapper {

    // EventMessage from DTO

    public static EventMessage fromDto(EventMessageDto eventMessageDto) {
        if (eventMessageDto == null)
            return null;

        EventMessage eventMessage = headerFromDto(eventMessageDto);

        if(eventMessageDto.getPayload() != null) {
            final PayloadDto payloadDto = eventMessageDto.getPayload();
            eventMessage.setTexts(Optional.ofNullable(payloadDto.getText())
                    .map(texts -> texts.stream()
                            .map(text -> EventMessageMapper.fromDto(text, eventMessage))
                            .collect(Collectors.toList()))
                    .orElse(null));
            eventMessage.setLinks(Optional.ofNullable(payloadDto.getLinks())
                    .map(links -> links.stream()
                            .map(link -> EventMessageMapper.fromDto(link, eventMessage))
                            .collect(Collectors.toList()))
                    .orElse(null));
            eventMessage.setRscKpis(Optional.ofNullable(payloadDto.getRscKpi())
                    .map(rscKpis -> rscKpis.stream()
                            .map(rscKpi -> EventMessageMapper.fromDto(rscKpi, eventMessage))
                            .collect(Collectors.toList()))
                    .orElse(null));
            eventMessage.setTimeseries(Optional.ofNullable(payloadDto.getTimeserie())
                    .map(timeseries -> timeseries.stream()
                            .map(timeserie -> EventMessageMapper.fromDto(timeserie, eventMessage))
                            .collect(Collectors.toList()))
                    .orElse(null));
        }
        return eventMessage;
    }

    public static EventMessage headerFromDto(EventMessageDto eventMessageDto) {
        if (eventMessageDto == null)
            return null;

        EventMessage eventMessage = new EventMessage();
        eventMessage.setId(null);
        eventMessage.setXmlns(eventMessageDto.getXmlns());

        if(eventMessageDto.getHeader() != null) {
            final HeaderDto headerDto = eventMessageDto.getHeader();
            eventMessage.setMessageId(headerDto.getMessageId());
            eventMessage.setNoun(headerDto.getNoun());
            eventMessage.setVerb(headerDto.getVerb());
            eventMessage.setTimestamp(headerDto.getTimestamp());
            eventMessage.setSource(headerDto.getSource());
            if(headerDto.getProperties() != null) {
                final PropertiesDto propertiesDto = headerDto.getProperties();
                eventMessage.setFormat(propertiesDto.getFormat());
                if(propertiesDto.getBusinessDataIdentifier() != null) {
                    final BusinessDataIdentifierDto businessDataIdentifierDto = propertiesDto.getBusinessDataIdentifier();
                    eventMessage.setBusinessApplication(businessDataIdentifierDto.getBusinessApplication().orElse(null));
                    eventMessage.setMessageType(businessDataIdentifierDto.getMessageType());
                    eventMessage.setMessageTypeName(businessDataIdentifierDto.getMessageTypeName());
                    eventMessage.setBusinessDayFrom(businessDataIdentifierDto.getBusinessDayFrom());
                    eventMessage.setBusinessDayTo(businessDataIdentifierDto.getBusinessDayTo());
                    eventMessage.setProcessStep(businessDataIdentifierDto.getProcessStep().orElse(null));
                    eventMessage.setTimeframe(businessDataIdentifierDto.getTimeframe().orElse(null));
                    eventMessage.setTimeframeNumber(businessDataIdentifierDto.getTimeframeNumber().orElse(null));
                    eventMessage.setSendingUser(businessDataIdentifierDto.getSendingUser().orElse(null));
                    eventMessage.setFileName(businessDataIdentifierDto.getFileName().orElse(""));
                    eventMessage.setTso(businessDataIdentifierDto.getTso().orElse(null));
                    eventMessage.setBiddingZone(businessDataIdentifierDto.getBiddingZone().orElse(null));
                    eventMessage.setEventMessageRecipients(businessDataIdentifierDto.getRecipients()
                            .map(recipients -> recipients.stream()
                                    .map(recipient -> EventMessageRecipient.builder()
                                            .eventMessage(eventMessage)
                                            .eicCode(recipient)
                                            .build())
                                    .collect(Collectors.toList()))
                            .orElse(null));
                    eventMessage.setCaseId(businessDataIdentifierDto.getCaseId().orElse(null));
                    eventMessage.setUniqueFileIdentifier(StringUtil.generateUniqueFileIdentifier(eventMessageDto));
                    eventMessage.setCoordinationStatus(businessDataIdentifierDto.getCoordinationStatus().orElse(null));
                    eventMessage.setEventMessageCoordinationComments(businessDataIdentifierDto.getCoordinationComments()
                            .map(coordinationCommentDtos -> coordinationCommentDtos.stream()
                                    .map(cc -> fromDto(cc, eventMessage))
                                    .collect(Collectors.toList()))
                            .orElse(null));
                }
            }
        }
        return eventMessage;
    }

    // Text

    private static EventMessageCoordinationComment fromDto(CoordinationCommentDto coordinationCommentDto,
                                                           EventMessage eventMessage) {
        if(coordinationCommentDto == null)
            return null;
        return EventMessageCoordinationComment.builder()
                .eicCode(coordinationCommentDto.getEicCode())
                .generalComment(coordinationCommentDto.getGeneralComment())
                .eventMessage(eventMessage)
                .build();
    }

    // Text

    private static Text fromDto(TextDataDto textDataDto, EventMessage eventMessage) {
        if(textDataDto == null)
            return null;
        return Text.builder()
                .name(textDataDto.getName())
                .value(textDataDto.getValue())
                .eventMessage(eventMessage)
                .build();
    }

    // Link

    private static Link fromDto(LinkDataDto linkDataDto, EventMessage eventMessage) {
        if(linkDataDto == null)
            return null;
        Link link = new Link();
        link.setName(linkDataDto.getName());
        link.setValue(linkDataDto.getValue());
        link.setLinkEicCodes(Optional.ofNullable(linkDataDto.getEicCode())
                .map(eicCodes -> eicCodes.stream()
                        .map(eicCode -> LinkEicCode.builder().link(link).eicCode(eicCode).build())
                        .collect(Collectors.toList()))
                .orElse(null));
        link.setEventMessage(eventMessage);
        return link;
    }

    // RscKpi

    private static RscKpi fromDto(RscKpiDataDto rscKpiDataDto, EventMessage eventMessage) {
        if (rscKpiDataDto == null)
            return null;
        RscKpi rscKpi = new RscKpi();
        rscKpi.setId(null);
        rscKpi.setName(rscKpiDataDto.getName());
        rscKpi.setJoinGraph(rscKpiDataDto.getJoinGraph());
        rscKpi.setRscKpiDatas(rscKpiDataDto.getData().stream()
                        .map(rscKpiData -> EventMessageMapper.fromDto(rscKpiData, rscKpi))
                        .collect(Collectors.toList()));
        rscKpi.setEventMessage(eventMessage);
        return rscKpi;
    }

    private static RscKpiData fromDto(RscKpiDataDetailsDto rscKpiDataDetailsDto, RscKpi rscKpi) {
        if (rscKpiDataDetailsDto == null)
            return null;
        RscKpiData rscKpiData = new RscKpiData();
        rscKpiData.setId(null);
        rscKpiData.setRscKpi(rscKpi);
        rscKpiData.setTimestamp(rscKpiDataDetailsDto.getTimestamp());
        rscKpiData.setGranularity(rscKpiDataDetailsDto.getGranularity());
        rscKpiData.setLabel(rscKpiDataDetailsDto.getLabel());
        rscKpiData.setRscKpiDataDetails(rscKpiDataDetailsDto.getDetail().stream()
                        .map(rscKpiDataDetails -> EventMessageMapper.fromDto(rscKpiDataDetails, rscKpiData))
                        .collect(Collectors.toList()));
        return rscKpiData;
    }

    private static RscKpiDataDetails fromDto(RscKpiTemporalDataDto rscKpiTemporalDataDto, RscKpiData rscKpiData) {
        if (rscKpiTemporalDataDto == null)
            return null;
        return RscKpiDataDetails.builder()
                .id(null)
                .value(Long.valueOf(rscKpiTemporalDataDto.getValue()))
                .eicCode(rscKpiTemporalDataDto.getEicCode())
                .rscKpiData(rscKpiData)
                .build();
    }

    // Timeserie

    private static Timeserie fromDto(TimeserieDataDto timeserieDataDto, EventMessage eventMessage) {
        if (timeserieDataDto == null)
            return null;
        Timeserie timeserie = new Timeserie();
        timeserie.setId(null);
        timeserie.setName(timeserieDataDto.getName());
        timeserie.setTimeserieDatas(timeserieDataDto.getData().stream()
                .map(timeserieDataDetailsDto -> EventMessageMapper.fromDto(timeserieDataDetailsDto, timeserie))
                .collect(Collectors.toList()));
        timeserie.setEventMessage(eventMessage);
        return timeserie;
    }

    private static TimeserieData fromDto(TimeserieDataDetailsDto timeserieDataDetailsDto, Timeserie timeserie) {
        if (timeserieDataDetailsDto == null)
            return null;
        TimeserieData timeserieData = new TimeserieData();
        timeserieData.setId(null);
        timeserieData.setTimeserie(timeserie);
        timeserieData.setTimestamp(timeserieDataDetailsDto.getTimestamp());
        timeserieData.setTimeserieDataDetailses(Optional.ofNullable(timeserieDataDetailsDto.getDetail())
                .map(timeserieDataDetails -> timeserieDataDetails.stream()
                        .filter(timeserieDataDetail -> timeserieDataDetail != null && timeserieDataDetail.getValue() != null)
                        .map(timeserieDataDetail -> EventMessageMapper.fromDto(timeserieDataDetail, timeserieData))
                        .collect(Collectors.toList()))
                .orElse(null));
        return timeserieData;
    }

    private static TimeserieDataDetails fromDto(TimeserieTemporalDataDto timeserieTemporalDataDto, TimeserieData timeserieData) {
        if (timeserieTemporalDataDto == null)
            return null;
        TimeserieDataDetails timeserieDataDetails = new TimeserieDataDetails();
        timeserieDataDetails.setId(null);
        timeserieDataDetails.setIdentifier(timeserieTemporalDataDto.getId());
        timeserieDataDetails.setLabel(timeserieTemporalDataDto.getLabel());
        timeserieDataDetails.setValue(timeserieTemporalDataDto.getValue());
        timeserieDataDetails.setTimeserieDataDetailsEicCodes(Optional.ofNullable(timeserieTemporalDataDto.getEicCode())
                .map(eicCodes -> eicCodes.stream()
                        .map(eicCode -> TimeserieDataDetailsEicCode.builder()
                                .timeserieDataDetails(timeserieDataDetails)
                                .eicCode(eicCode)
                                .build())
                        .collect(Collectors.toList()))
                .orElse(null));
        timeserieDataDetails.setTimeserieDataDetailsResults(Optional.ofNullable(timeserieTemporalDataDto.getResults())
                .map(resultDtos -> resultDtos.stream()
                        .map(resultDto -> TimeserieDataDetailsResult.builder()
                                .timeserieDataDetails(timeserieDataDetails)
                                .eicCode(resultDto.getEicCode())
                                .answer(resultDto.getAnswer())
                                .explanation(resultDto.getExplanation())
                                .comment(resultDto.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .orElse(null));
        timeserieDataDetails.setTimeserieData(timeserieData);
        return timeserieDataDetails;
    }

    public static EventMessageDto buildEventMessageDtoLightForCoordination(EventMessage eventMessage) {
        return EventMessageDto.builder()
                .header(HeaderDto.builder()
                        .properties(PropertiesDto.builder()
                                .businessDataIdentifier(BusinessDataIdentifierDto.builder()
                                        .businessDayFrom(eventMessage.getBusinessDayFrom())
                                        .businessDayTo(eventMessage.getBusinessDayTo())
                                        .sendingUser(eventMessage.getSendingUser())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    // EventMessage to DTO

    public static EventMessageDto toDto(EventMessage eventMessage) {
        if (eventMessage == null)
            return null;

        EventMessageDto eventMessageDto = new EventMessageDto();
        eventMessageDto.setXmlns(eventMessage.getXmlns());

        HeaderDto headerDto = eventMessageDto.getHeader();
        headerDto.setMessageId(eventMessage.getMessageId());
        headerDto.setNoun(eventMessage.getNoun());
        headerDto.setVerb(eventMessage.getVerb());
        headerDto.setTimestamp(eventMessage.getTimestamp());
        headerDto.setSource(eventMessage.getSource());

        PropertiesDto propertiesDto = headerDto.getProperties();
        propertiesDto.setFormat(eventMessage.getFormat());

        BusinessDataIdentifierDto businessDataIdentifierDto = propertiesDto.getBusinessDataIdentifier();
        businessDataIdentifierDto.setBusinessApplication(eventMessage.getBusinessApplication());
        businessDataIdentifierDto.setMessageType(eventMessage.getMessageType());
        businessDataIdentifierDto.setMessageTypeName(eventMessage.getMessageTypeName());
        businessDataIdentifierDto.setBusinessDayFrom(eventMessage.getBusinessDayFrom());
        businessDataIdentifierDto.setBusinessDayTo(eventMessage.getBusinessDayTo());
        businessDataIdentifierDto.setProcessStep(eventMessage.getProcessStep());
        businessDataIdentifierDto.setTimeframe(eventMessage.getTimeframe());
        businessDataIdentifierDto.setTimeframeNumber(eventMessage.getTimeframeNumber());
        businessDataIdentifierDto.setSendingUser(eventMessage.getSendingUser());
        businessDataIdentifierDto.setFileName(eventMessage.getFileName());
        businessDataIdentifierDto.setTso(eventMessage.getTso());
        businessDataIdentifierDto.setBiddingZone(eventMessage.getBiddingZone());
        businessDataIdentifierDto.setRecipients(
                CollectionUtils.isNotEmpty(eventMessage.getEventMessageRecipients())
                        ? eventMessage.getEventMessageRecipients().stream()
                        .map(EventMessageRecipient::getEicCode)
                        .collect(Collectors.toList())
                        : null
        );
        businessDataIdentifierDto.setCaseId(eventMessage.getCaseId());
        businessDataIdentifierDto.setCoordinationStatus(eventMessage.getCoordinationStatus());
        businessDataIdentifierDto.setCoordinationComments(
                CollectionUtils.isNotEmpty(eventMessage.getEventMessageCoordinationComments())
                        ? eventMessage.getEventMessageCoordinationComments().stream()
                        .map(cc -> CoordinationCommentDto.builder().eicCode(cc.getEicCode()).generalComment(cc.getGeneralComment()).build())
                        .collect(Collectors.toList())
                        : null
        );

        PayloadDto payloadDto = eventMessageDto.getPayload();
        payloadDto.setText(CollectionUtils.isNotEmpty(eventMessage.getTexts())
                ? eventMessage.getTexts().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);
        payloadDto.setLinks(CollectionUtils.isNotEmpty(eventMessage.getLinks())
                ? eventMessage.getLinks().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);
        payloadDto.setRscKpi(CollectionUtils.isNotEmpty(eventMessage.getRscKpis())
                ? eventMessage.getRscKpis().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);
        payloadDto.setTimeserie(CollectionUtils.isNotEmpty(eventMessage.getTimeseries())
                ? eventMessage.getTimeseries().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);

        return eventMessageDto;
    }

    // Text

    private static TextDataDto toDto(Text text) {
        if(text == null)
            return null;
        TextDataDto textDataDto = new TextDataDto();
        textDataDto.setName(text.getName());
        textDataDto.setValue(text.getValue());
        return textDataDto;
    }

    // Link

    private static LinkDataDto toDto(Link link) {
        if(link == null)
            return null;
        LinkDataDto linkDataDto = new LinkDataDto();
        linkDataDto.setName(link.getName());
        linkDataDto.setValue(link.getValue());
        linkDataDto.setEicCode(CollectionUtils.isNotEmpty(link.getLinkEicCodes())
                ? link.getLinkEicCodes().stream().map(LinkEicCode::getEicCode).collect(Collectors.toList())
                : null);
        return linkDataDto;
    }

    // RscKpi

    private static RscKpiDataDto toDto(RscKpi rscKpi) {
        if (rscKpi == null)
            return null;
        RscKpiDataDto rscKpiDataDto = new RscKpiDataDto();
        rscKpiDataDto.setName(rscKpi.getName());
        rscKpiDataDto.setJoinGraph(rscKpi.getJoinGraph());
        rscKpiDataDto.setData(CollectionUtils.isNotEmpty(rscKpi.getRscKpiDatas())
                ? rscKpi.getRscKpiDatas().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);
        return rscKpiDataDto;
    }

    private static RscKpiDataDetailsDto toDto(RscKpiData rscKpiData) {
        if (rscKpiData == null)
            return null;
        RscKpiDataDetailsDto rscKpiDataDetailsDto = new RscKpiDataDetailsDto();
        rscKpiDataDetailsDto.setTimestamp(rscKpiData.getTimestamp());
        rscKpiDataDetailsDto.setGranularity(rscKpiData.getGranularity());
        rscKpiDataDetailsDto.setLabel(rscKpiData.getLabel());
        rscKpiDataDetailsDto.setDetail(CollectionUtils.isNotEmpty(rscKpiData.getRscKpiDataDetails())
                ? rscKpiData.getRscKpiDataDetails().stream().map(EventMessageMapper::toDto).collect(Collectors.toList())
                : null);
        return rscKpiDataDetailsDto;
    }

    private static RscKpiTemporalDataDto toDto(RscKpiDataDetails rscKpiDataDetails) {
        if (rscKpiDataDetails == null)
            return null;
        RscKpiTemporalDataDto rscKpiTemporalDataDto = new RscKpiTemporalDataDto();
        rscKpiTemporalDataDto.setValue(Optional.ofNullable(rscKpiDataDetails.getValue()).map(Long::intValue).orElse(null));
        rscKpiTemporalDataDto.setEicCode(rscKpiDataDetails.getEicCode());
        return rscKpiTemporalDataDto;
    }

    // Timeserie

    private static TimeserieDataDto toDto(Timeserie timeserie) {
        if (timeserie == null)
            return null;
        TimeserieDataDto timeserieDataDto = new TimeserieDataDto();
        timeserieDataDto.setName(timeserie.getName());
        timeserieDataDto.setData(CollectionUtils.isNotEmpty(timeserie.getTimeserieDatas())
                ? timeserie.getTimeserieDatas().stream()
                .map(EventMessageMapper::toDto)
                .collect(Collectors.toList())
                : null);
        return timeserieDataDto;
    }

    private static TimeserieDataDetailsDto toDto(TimeserieData timeserieData) {
        if (timeserieData == null)
            return null;
        TimeserieDataDetailsDto timeserieDataDetailsDto = new TimeserieDataDetailsDto();
        timeserieDataDetailsDto.setTimestamp(timeserieData.getTimestamp());
        timeserieDataDetailsDto.setDetail(CollectionUtils.isNotEmpty(timeserieData.getTimeserieDataDetailses())
                ? timeserieData.getTimeserieDataDetailses().stream()
                .map(EventMessageMapper::toDto)
                .collect(Collectors.toList())
                : null);
        return timeserieDataDetailsDto;
    }

    private static TimeserieTemporalDataDto toDto(TimeserieDataDetails timeserieDataDetails) {
        if (timeserieDataDetails == null)
            return null;
        TimeserieTemporalDataDto timeserieTemporalDataDto = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto.setId(timeserieDataDetails.getIdentifier());
        timeserieTemporalDataDto.setLabel(timeserieDataDetails.getLabel());
        timeserieTemporalDataDto.setValue(timeserieDataDetails.getValue());
        timeserieTemporalDataDto.setEicCode(CollectionUtils.isNotEmpty(timeserieDataDetails.getTimeserieDataDetailsEicCodes())
                ? timeserieDataDetails.getTimeserieDataDetailsEicCodes().stream()
                .map(TimeserieDataDetailsEicCode::getEicCode)
                .collect(Collectors.toList())
                : null
        );
        timeserieTemporalDataDto.setResults(CollectionUtils.isNotEmpty(timeserieDataDetails.getTimeserieDataDetailsResults())
                ? timeserieDataDetails.getTimeserieDataDetailsResults().stream()
                .map(result -> TimeserieOutputResultDto.builder()
                        .eicCode(result.getEicCode())
                        .answer(result.getAnswer())
                        .explanation(result.getExplanation())
                        .comment(result.getComment())
                        .build())
                .collect(Collectors.toList())
                : null
        );
        return timeserieTemporalDataDto;
    }

}
