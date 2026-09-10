package com.bloom.common.exception;

public class InsufficientStockException extends BloomException {

    public InsufficientStockException(String message) {
        super(409, "Conflict", message);
    }

    public InsufficientStockException(String sku, int requested, int available) {
        super(
                409,
                "Conflict",
                String.format("Insufficient stock for SKU '%s'. Requested: %d, Available: %d", sku, requested, available)
        );
    }
}
