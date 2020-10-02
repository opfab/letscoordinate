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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static final String PROCESS_SUCCESS = "ProcessSuccess";
    public static final String PROCESS_FAILED = "ProcessFailed";
    public static final String PROCESS_ACTION = "ProcessAction";
    public static final String PROCESS_INFORMATION = "ProcessInformation";
    public static final String MESSAGE_VALIDATED = "DfgMessageValidated";

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
}
