package org.lfenergy.letscoordinate.backend.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IgnoreProcessException extends IllegalStateException {

    public IgnoreProcessException(String message) {
        super(message);
    }
}
