-- Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE timeserie (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT NOT NULL,
                name VARCHAR(100) NOT NULL,
                coordination_status VARCHAR(10),
                PRIMARY KEY (id)
);

CREATE TABLE timeserie_data (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_timeserie BIGINT NOT NULL,
                timestamp DATETIME NOT NULL,
                PRIMARY KEY (id)
);

CREATE TABLE timeserie_data_details (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_timeserie_data BIGINT NOT NULL,
                identifier VARCHAR(50),
                label VARCHAR(50),
                value VARCHAR(1000) NOT NULL,
                accept INT,
                reject INT,
                explanation VARCHAR(500),
                comment VARCHAR(500),
                PRIMARY KEY (id)
);

CREATE TABLE timeserie_data_details_eic_code (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_timeserie_data_details BIGINT NOT NULL,
                eic_code VARCHAR(20) NOT NULL,
                PRIMARY KEY (id)
);

ALTER TABLE timeserie ADD CONSTRAINT event_message_timeserie_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE timeserie_data ADD CONSTRAINT timeserie_timeserie_data_fk
FOREIGN KEY (id_timeserie)
REFERENCES timeserie (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE timeserie_data_details ADD CONSTRAINT timeserie_data_timeserie_data_details_fk
FOREIGN KEY (id_timeserie_data)
REFERENCES timeserie_data (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE timeserie_data_details_eic_code ADD CONSTRAINT timeserie_data_details_timeserie_data_details_eic_code_fk
FOREIGN KEY (id_timeserie_data_details)
REFERENCES timeserie_data_details (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;