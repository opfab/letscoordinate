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

import org.lfenergy.letscoordinate.backend.config.LetscoProperties;

public class ApplicationContextUtil {

    public static LetscoProperties initLetscoProperties() {
        LetscoProperties letscoProperties = new LetscoProperties();

        LetscoProperties.InputFile.Validation validation = new LetscoProperties.InputFile.Validation();
        validation.setAcceptPropertiesIgnoreCase(true);
        validation.setFailOnUnknownProperties(false);

        LetscoProperties.InputFile inputFile = new LetscoProperties.InputFile();
        inputFile.setDir("src/generateRscKpiExcelReport/resources");
        inputFile.setValidation(validation);

        letscoProperties.setInputFile(inputFile);
        return letscoProperties;
    }

}
