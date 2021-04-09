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
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TextDataDto;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class StringUtilTest {

    @Test
    public void cleanAndSplitString_emptyInputs() {
        assertNull(StringUtil.cleanAndSplitString(null, null));
        assertNull(StringUtil.cleanAndSplitString("input string", null));
    }

    @Test
    public void cleanAndSplitString_inputNotContainingSeparator() {
        String input = "boo; \nand; \nfoo   ";
        String separator = "#";
        assertEquals(Collections.singletonList(input), StringUtil.cleanAndSplitString(input, separator));
    }

    @Test
    public void cleanAndSplitString_inputContainingSeparator() {
        String input = "boo; \nand; \nfoo   ";
        String separator = ";";
        assertEquals(Arrays.asList("boo", "and", "foo"), StringUtil.cleanAndSplitString(input, separator));
    }

    @Test
    public void objectToJson_withoutPrettyFormat() {
        TextDataDto textDataDto = new TextDataDto();
        textDataDto.setName("comment");
        textDataDto.setValue("j aime la tourte");
        assertEquals("{\"name\":\"comment\",\"value\":\"j aime la tourte\"}", StringUtil.objectToJson(textDataDto));
    }

    @Test
    public void objectToJson_withPrettyFormat() {
        TextDataDto textDataDto = new TextDataDto();
        textDataDto.setName("comment");
        textDataDto.setValue("j aime la tourte");
        assertNotEquals("{\"name\":\"comment\",\"value\":\"j aime la tourte\"}", StringUtil.objectToJson(textDataDto, true));
    }

    @Test
    public void toCamelCase_emptyInput() {
        String input = null;
        assertEquals(input, StringUtil.toCamelCase(input));

        input = "";
        assertEquals(input, StringUtil.toCamelCase(input));
    }

    @Test
    public void toCamelCase_validInput() {
        String input = "  taTa_tOTo- TiTi   ";
        assertEquals("tataTotoTiti", StringUtil.toCamelCase(input));
    }

    @Test
    public void toLowercaseIdentifier_emptyInput() {
        String input = null;
        assertEquals(input, StringUtil.toLowercaseIdentifier(input));

        input = "";
        assertEquals(input, StringUtil.toLowercaseIdentifier(input));
    }

    @Test
    public void toLowercaseIdentifier_validInput() {
        String input = "  taTa_tOTo- TiTi   ";
        assertEquals("tatatototiti", StringUtil.toLowercaseIdentifier(input));
    }

    @Test
    public void getFilenameWithoutExtension_containsExtension() {
        String input = "file1234.2021.txt";
        assertEquals("file1234.2021", StringUtil.getFilenameWithoutExtension(input));
    }

    @Test
    public void getFilenameWithoutExtension_notContainingExtension() {
        String input = "file1234_2021";
        assertEquals("file1234_2021", StringUtil.getFilenameWithoutExtension(input));
    }

}
