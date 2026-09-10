package com.bloom.common.exception;

public class EntityNotFoundException extends BloomException {

    public EntityNotFoundException(String message) {
        super(404, "Not Found", message);
    }

    public EntityNotFoundException(String entityName, Object identifier) {
        super(404, "Not Found", String.format("%s with identifier '%s' was not found", entityName, identifier));
    }
}
