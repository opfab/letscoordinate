-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

ALTER TABLE coordination_ra ADD COLUMN id_timeserie_data BIGINT NOT NULL;

ALTER TABLE event_message ADD COLUMN xmlns VARCHAR(250);

ALTER TABLE event_message_file ADD COLUMN file_name VARCHAR(1000) NOT NULL;

ALTER TABLE event_message_file ADD COLUMN file_type VARCHAR(20) NOT NULL;

ALTER TABLE event_message_file ADD COLUMN file_direction VARCHAR(20) NOT NULL;

ALTER TABLE event_message_file ADD COLUMN creation_date DATETIME(3) NOT NULL;

ALTER TABLE event_message_file RENAME COLUMN file TO file_content;