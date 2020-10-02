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

package org.lfenergy.letscoordinate.dataprovider.enums;

import lombok.Getter;

public enum ProvidedDataTypeEnum {
    PROCESS_SUCCESS_INPUT_DATA("process_success", "letsco-data-provider\\src\\main\\resources\\static\\kafka\\ProcessSuccessInputData.json"),
    PROCESS_FAILED_INPUT_DATA("process_failed", "letsco-data-provider\\src\\main\\resources\\static\\kafka\\ProcessFailedInputData.json"),
    MESSAGE_VALIDATED_INPUT_DATA("message_validated", "letsco-data-provider\\src\\main\\resources\\static\\kafka\\MessageValidatedInputData.json");

    ProvidedDataTypeEnum(String topicName, String path) {
        this.topicName = topicName;
        this.path = path;
    }

    @Getter
    private String topicName;
    @Getter
    private String path;
}
