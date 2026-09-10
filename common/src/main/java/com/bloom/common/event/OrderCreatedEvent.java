package com.bloom.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId,
                String orderNumber,
                UUID customerId,
                String customerEmail,
                List<OrderItemPayload> items,
                AddressSnapshot shippingAddress,
                BigDecimal totalAmount,
                Instant createdAt) {

        public record OrderItemPayload(
                        UUID variantId,
                        String sku,
                        String productName,
                        BigDecimal unitPrice,
                        int quantity,
                        BigDecimal totalPrice) {
        }

        public record AddressSnapshot(
                        String recipientName,
                        String phoneNumber,
                        String streetLine1,
                        String streetLine2,
                        String city,
                        String stateProvince,
                        String postalCode,
                        String countryCode) {
        }
}
