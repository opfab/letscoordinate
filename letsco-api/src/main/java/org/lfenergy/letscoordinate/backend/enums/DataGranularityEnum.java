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
}
