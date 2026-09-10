package com.bloom.common.exception;

public class BadRequestException extends BloomException {

    public BadRequestException(String message) {
        super(400, "Bad Request", message);
    }
}
