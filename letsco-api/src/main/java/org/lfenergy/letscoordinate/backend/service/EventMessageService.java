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

package org.lfenergy.letscoordinate.backend.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.config.CoordinationConfig;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.*;
import org.lfenergy.letscoordinate.backend.exception.InvalidInputFileException;
import org.lfenergy.letscoordinate.backend.util.Constants;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventMessageService {

    private final CoordinationConfig coordinationConfig;

    public void checkEicCodes(EventMessageDto eventMessageDto) throws InvalidInputFileException {
        // check that all eic_code provided by the dto exists in our database
        Set<String> dtoEicCodes = extratEicCodesFromEventMessageDto(eventMessageDto);
        List<String> knownEicCodes = getKnownEicCodes(dtoEicCodes);
        knownEicCodes.addAll(Arrays.asList(Constants.ALL_RSCS_CODE, Constants.ALL_REGIONS_CODE));
        Set<String> unknownEicCodes = dtoEicCodes.stream()
                .filter(eicCode -> !knownEicCodes.contains(eicCode))
                .collect(Collectors.toSet());
        if(!unknownEicCodes.isEmpty()) {
            // TODO monitoring
            throw new InvalidInputFileException("Unknown eic_codes found! >>> " + unknownEicCodes.toString());
        }
    }

    private List<String> getKnownEicCodes(Set<String> eicCodes) {
        return coordinationConfig.getAllEicCodes().stream()
                .filter(eicCode -> eicCodes.contains(eicCode))
                .collect(Collectors.toList());
    }

    private Set<String> extratEicCodesFromEventMessageDto(EventMessageDto eventMessageDto) {
        Set<String> eicCodes = new HashSet<>();
        if(eventMessageDto == null)
            return eicCodes;

        HeaderDto headerDto = eventMessageDto.getHeader();
        if(headerDto != null && headerDto.getProperties() != null && headerDto.getProperties().getBusinessDataIdentifier() != null) {
            BusinessDataIdentifierDto businessDataIdentifierDto = headerDto.getProperties().getBusinessDataIdentifier();
            businessDataIdentifierDto.getSendingUser().ifPresent(eicCodes::add);
            businessDataIdentifierDto.getTso().ifPresent(eicCodes::add);
            businessDataIdentifierDto.getBiddingZone().ifPresent(eicCodes::add);
        }

        PayloadDto payloadDto = eventMessageDto.getPayload();
        if(payloadDto != null) {
            if(payloadDto.getLinks() != null) {
                eicCodes.addAll(payloadDto.getLinks().stream()
                        .filter(Objects::nonNull)
                        .map(LinkDataDto::getEicCode)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
            if(payloadDto.getRscKpi() != null) {
                eicCodes.addAll(payloadDto.getRscKpi().stream()
                        .filter(Objects::nonNull)
                        .map(RscKpiDataDto::getData)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(RscKpiDataDetailsDto::getDetail)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(RscKpiTemporalDataDto::getEicCode)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
            if(payloadDto.getTimeserie() != null) {
                eicCodes.addAll(payloadDto.getTimeserie().stream()
                        .filter(Objects::nonNull)
                        .map(TimeserieDataDto::getData)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(TimeserieDataDetailsDto::getDetail)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .map(TimeserieTemporalDataDto::getEicCode)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        }

        return eicCodes;
    }

}
