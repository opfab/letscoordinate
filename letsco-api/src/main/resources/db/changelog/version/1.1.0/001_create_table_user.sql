-- Copyright (c) 2020, RTE (https://www.rte-france.com)
-- Copyright (c) 2020 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    eic_code VARCHAR(20),
    PRIMARY KEY (ID)
);

CREATE TABLE user_service (
    id BIGINT AUTO_INCREMENT NOT NULL,
    id_user BIGINT NOT NULL,
    service_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (ID)
);

ALTER TABLE user_service ADD CONSTRAINT user_user_service_fk
FOREIGN KEY (id_user)
REFERENCES user (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION;