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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PojoUtil {

    private static final String MULTIPLE_ELEMENTS_SEPARATOR = ";";

    /**
     * Allows to set a given fieldValue to a given object's field by fieldName
     *
     * @param object the object to update
     * @param fieldName the fieldName
     * @param fieldValue the fieldValue
     * @throws NoSuchFieldException if no field found matching the fieldName
     * @throws IllegalAccessException if the found object'sfield is not accessible (this exception is resolved internally,
     * so it's never thrown but should be declared)
     */
    public static void setProperty(Object object, String fieldName, String fieldValue) throws NoSuchFieldException, IllegalAccessException {
        if (object == null || fieldName == null || fieldValue == null)
            return;
        Field field = object.getClass().getDeclaredField(fieldName);
        boolean fieldOriginAccessibility = field.isAccessible();
        field.setAccessible(true);
        // Primitive types
        if (field.getType() == Character.TYPE || field.getType() == Character.class) {field.set(object, fieldValue.charAt(0));}
        else if (field.getType() == Short.TYPE || field.getType() == Short.class) {field.set(object, Short.parseShort(fieldValue));}
        else if (field.getType() == Integer.TYPE || field.getType() == Integer.class) {field.set(object, Integer.parseInt(fieldValue));}
        else if (field.getType() == Long.TYPE || field.getType() == Long.class) {field.set(object, Long.parseLong(fieldValue));}
        else if (field.getType() == Float.TYPE || field.getType() == Float.class) {field.set(object, Float.parseFloat(fieldValue));}
        else if (field.getType() == Double.TYPE || field.getType() == Double.class) {field.set(object, Double.parseDouble(fieldValue));}
        else if (field.getType() == Byte.TYPE || field.getType() == Byte.class) {field.set(object, Byte.parseByte(fieldValue));}
        else if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {field.set(object, Boolean.parseBoolean(fieldValue));}
        // Other types
        else if (field.getType() == String.class) {field.set(object, fieldValue);}
        else if (field.getType() == OffsetDateTime.class) {
            field.set(object, DateUtil.toOffsetDateTime(fieldValue));
        } else if (field.getType() == List.class) {
            field.set(object, StringUtil.cleanAndSplitString(fieldValue, MULTIPLE_ELEMENTS_SEPARATOR));
        } else {
            throw new RuntimeException("Unable to map field \"" + object.getClass().getSimpleName() + "." + fieldName + "\" of type \""
                    + field.getType().getSimpleName() + "\"");
        }
        field.setAccessible(fieldOriginAccessibility);
    }

}

