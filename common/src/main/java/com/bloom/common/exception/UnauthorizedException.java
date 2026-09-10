package com.bloom.common.exception;

public class UnauthorizedException extends BloomException {

    public UnauthorizedException(String message) {
        super(401, "Unauthorized", message);
    }

    public UnauthorizedException() {
        super(401, "Unauthorized", "Authentication is required to access this resource");
    }
}
