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

package org.lfenergy.letscoordinate.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.BusinessDataIdentifierDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.header.HeaderDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    /**
     * <p>This function allows to split an input string according to a separator after removing all spaces from it
     * if and only if the input string contains the separator</p>
     * <p>If the input string does not contain the separator, it is returned as is</p>
     * <p>If the input string is null or the separator is null, then this function returns null</p>
     *
     * <p> The string {@code "boo; \nand; \nfoo   "}, for example, yields the following results
     * with these expressions:
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
     * <tr>
     *  <th>Separator</th>
     *  <th>Result</th>
     * </tr>
     * <tr><td align=center>";"</td>
     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td align=center>" "</td>
     *     <td>{@code { "boo;", "and;", "foo" }}</td></tr>
     * <tr><td align=center>"#"</td>
     *     <td>{@code { "boo; \nand; \nfoo   " }}</td></tr>
     * </table></blockquote>
     *
     * @param inputString the input string
     * @param separator the separator
     * @return list of string
     */
    public static List<String> cleanAndSplitString(final String inputString, final String separator) {
        if (inputString == null || separator == null)
            return null;
        else if (!inputString.contains(separator))
            return Collections.singletonList(inputString);
        else return Stream.of(inputString.trim().replaceAll("\\s+", separator).split(separator))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
    }

    public static String objectToJson(Object obj) {
        return objectToJson(obj, false);
    }

    public static String objectToJson(Object obj, boolean prettyFormat) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            return prettyFormat ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public static String toCamelCase(String str) {
        if (StringUtils.isBlank(str))
            return str;
        String result = "";
        List<String> tokens = Arrays.asList(str.trim().split("[ _\\-]"));
        for(String token : tokens) {
            result += StringUtils.capitalize(token.toLowerCase());
        }
        return StringUtils.uncapitalize(result);
    }

    public static String toLowercaseIdentifier(String str) {
        if (StringUtils.isBlank(str))
            return str;
        return toCamelCase(str).toLowerCase();
    }

    public static String getFilenameWithoutExtension(String filename) {
        return filename.contains(".") ? filename.substring(0, filename.lastIndexOf(".")) : filename;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return null;
        String[] tokens = fileName.split("[.]");
        return tokens[tokens.length - 1];
    }

    public static String generateUniqueFileIdentifier(EventMessageDto eventMessageDto) {
        HeaderDto headerDto = eventMessageDto.getHeader();
        BusinessDataIdentifierDto bdi = headerDto.getProperties().getBusinessDataIdentifier();
        String eventMessageDtoStr = new StringBuilder()
                .append("verb").append( headerDto.getVerb())
                .append("noun").append( headerDto.getNoun())
                .append("timestamp").append( headerDto.getTimestamp())
                .append("source").append( headerDto.getSource())
                .append("messageId").append( headerDto.getMessageId())
                .append("format").append( headerDto.getProperties().getFormat())
                .append("messageType").append( bdi.getMessageType())
                .append("messageTypeName").append( bdi.getMessageTypeName())
                .append("businessDayFrom").append( bdi.getBusinessDayFrom())
                .append("businessDayTo").append( bdi.getBusinessDayTo())
                .append("processStep").append( bdi.getProcessStepSimple())
                .append("timeframe").append( bdi.getTimeframeSimple())
                .append("businessApplication").append( bdi.getBusinessApplicationSimple())
                .append("timeframeNumber").append( bdi.getTimeframeNumberSimple())
                .append("sendingUser").append( bdi.getSendingUserSimple())
                .append("caseId").append( bdi.getCaseIdSimple())
                .append("fileName").append( bdi.getFileNameSimple())
                .append("tso").append( bdi.getTsoSimple())
                .append("biddingZone").append( bdi.getBiddingZoneSimple())
                .toString();
        return UUID.nameUUIDFromBytes(eventMessageDtoStr.getBytes()).toString();
    }

}
