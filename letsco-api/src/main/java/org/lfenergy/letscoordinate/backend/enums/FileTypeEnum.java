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

package org.lfenergy.letscoordinate.backend.enums;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FileTypeEnum {
    JSON("json"), EXCEL("excel", "xls", "xlsx"), UNKNOWN;

    private FileTypeEnum(String... extensions) {
        this.extensions = Stream.of(extensions).collect(Collectors.toSet());
    }

    @Getter
    private Set<String> extensions;

    public static FileTypeEnum getByExtensionIgnoreCase(String name) {
        for (FileTypeEnum value : values()) {
            if (value.extensions.contains(name != null ? name.toLowerCase() : name))
                return value;
        }
        return UNKNOWN;
    }
}
