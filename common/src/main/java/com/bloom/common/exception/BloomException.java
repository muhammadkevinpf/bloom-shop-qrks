package com.bloom.common.exception;

public abstract class BloomException extends RuntimeException {

    private final int statusCode;
    private final String error;

    public BloomException(int statusCode, String error, String message) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
    }

    public BloomException(int statusCode, String error, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }
}
