-- Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
-- Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
-- See AUTHORS.txt
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
-- SPDX-License-Identifier: MPL-2.0
-- This file is part of the Let’s Coordinate project.

-- PLEASE NOTE: dates should be in UTC timezone and 'yyyy-MM-dd HH:mm:ss.SSS' format

delete from event_message
where noun = 'DfgMessageValidated'
and source = 'ServiceA'
and message_type_name = 'Validation'
and sending_user = '10XPT-REN------9'
and timestamp >= '2021-08-10 14:32:19.000'
and timestamp <= '2021-08-25 12:32:19.000';