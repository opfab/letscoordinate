package org.lfenergy.letscoordinate.backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataGranularityEnum {
    DAILY("D"), YEARLY("Y");

    @Getter
    @JsonValue
    private String value;

    public static DataGranularityEnum getByValue(String value) {
        if (value == null)
            return null;
        for (DataGranularityEnum dataGranularityEnum : values()) {
            if (dataGranularityEnum.getValue().equals(value))
                return dataGranularityEnum;
        }
        return null;
    }
}
