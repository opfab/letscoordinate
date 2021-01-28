package org.lfenergy.letscoordinate.backend.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PositiveTechnicalQualityCheckException extends IllegalStateException {

    public PositiveTechnicalQualityCheckException(String message) {
        super(message);
    }
}

