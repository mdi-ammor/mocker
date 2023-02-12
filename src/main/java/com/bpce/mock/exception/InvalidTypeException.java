package com.bpce.mock.exception;

import lombok.Data;

@Data
public class InvalidTypeException extends Exception {
    private String field;
    private String type;

    public InvalidTypeException(String field, String type) {
        super(String.format("Invalid type for the field %s : %s", field, type));
        this.field = field;
        this.type = type;
    }
}
