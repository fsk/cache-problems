package com.lab.distributed.exception;

public class LabException extends RuntimeException {

    private final String code;

    public LabException(String code, String message) {
        super(message);
        this.code = code;
    }

    public LabException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
