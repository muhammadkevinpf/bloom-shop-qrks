package com.bloom.common.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservedEvent(UUID orderId,
        boolean success,
        String failureReason,
        Instant timestamp) {

    public static InventoryReservedEvent success(UUID orderId) {
        return new InventoryReservedEvent(orderId, true, null, Instant.now());
    }

    public static InventoryReservedEvent failure(UUID orderId, String reason) {
        return new InventoryReservedEvent(orderId, false, reason, Instant.now());
    }
}
