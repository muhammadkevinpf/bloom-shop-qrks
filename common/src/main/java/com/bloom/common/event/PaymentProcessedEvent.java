package com.bloom.common.event;

import java.util.UUID;

public record PaymentProcessedEvent(UUID orderId, UUID transactionId, String status) {

}
