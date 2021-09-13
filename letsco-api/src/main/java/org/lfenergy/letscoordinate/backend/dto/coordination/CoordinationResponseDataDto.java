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

package org.lfenergy.letscoordinate.backend.dto.coordination;

import lombok.*;
import org.lfenergy.letscoordinate.backend.enums.CoordinationEntityRaResponseEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationResponseDataDto {
    private Long validationDataId;
    private String generalComment;
    private List<FormData> formData;

    @Getter
    @Setter
    @Builder
    public static class FormData {
        private Long id;
        private CoordinationEntityRaResponseEnum response;
        private String explanation;
        private String comment;
    }

    public Map<Long, FormData> formDataMap() {
        return Optional.ofNullable(formData)
                .map(fd -> fd.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(FormData::getId, Function.identity())))
                .orElse(new HashMap<>());
    }
}
