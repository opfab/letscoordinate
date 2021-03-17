package org.lfenergy.letscoordinate.backend.exception;

public class JsonDataMandatoryFieldNullException extends IllegalArgumentException {

    public JsonDataMandatoryFieldNullException(String field) {
        super("The mandatory field " + field + " is null");
    }
}
