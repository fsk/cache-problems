package com.lab.distributed.exception;

public class ResourceNotFoundException extends LabException {

    public ResourceNotFoundException(String resource, Object id) {
        super("NOT_FOUND", resource + " not found: " + id);
    }
}
