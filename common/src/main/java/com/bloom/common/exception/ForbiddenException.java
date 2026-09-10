package com.bloom.common.exception;

public class ForbiddenException extends BloomException {

    public ForbiddenException(String message) {
        super(403, "Forbidden", message);
    }

    public ForbiddenException() {
        super(403, "Forbidden", "You do not have permission to perform this action");
    }
}
