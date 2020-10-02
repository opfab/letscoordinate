-- Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE event_message (
    id BIGINT AUTO_INCREMENT NOT NULL,
    message_id VARCHAR(50) NOT NULL,
    noun VARCHAR(100) NOT NULL,
    verb VARCHAR(20) NOT NULL,
    timestamp DATETIME(3) NOT NULL,
    source VARCHAR(100) NOT NULL,
    format VARCHAR(10) NOT NULL,
    message_type VARCHAR(10),
    message_type_name VARCHAR(100) NOT NULL,
    business_day_from DATETIME(3) NOT NULL,
    business_day_to DATETIME(3),
    process_step VARCHAR(20),
    timeframe VARCHAR(5),
    timeframe_number INTEGER,
    sending_user VARCHAR(20),
    file_name VARCHAR(100),
    tso VARCHAR(20),
    bidding_zone VARCHAR(20),
    PRIMARY KEY (ID)
);