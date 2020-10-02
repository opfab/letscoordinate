-- Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE rsc_kpi (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_event_message BIGINT NOT NULL,
                name VARCHAR(10) NOT NULL,
                PRIMARY KEY (id)
);


CREATE TABLE rsc_kpi_data (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_rsc_kpi BIGINT NOT NULL,
                timestamp DATETIME(3) NOT NULL,
                label VARCHAR(50),
                PRIMARY KEY (id)
);


CREATE TABLE rsc_kpi_data_details (
                id BIGINT AUTO_INCREMENT NOT NULL,
                id_rsc_kpi_data BIGINT NOT NULL,
                value BIGINT NOT NULL,
                eic_code VARCHAR(20),
                PRIMARY KEY (id)
);


ALTER TABLE rsc_kpi ADD CONSTRAINT event_message_rsc_kpi_fk
FOREIGN KEY (id_event_message)
REFERENCES event_message (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE rsc_kpi_data ADD CONSTRAINT rsc_kpi_rsc_kpi_data_fk
FOREIGN KEY (id_rsc_kpi)
REFERENCES rsc_kpi (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE rsc_kpi_data_details ADD CONSTRAINT rsc_kpi_data_rsc_kpi_data_details_fk
FOREIGN KEY (id_rsc_kpi_data)
REFERENCES rsc_kpi_data (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;