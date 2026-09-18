package com.bloom.order.dto;

import com.bloom.common.event.OrderCreatedEvent.AddressSnapshot;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull AddressSnapshot shippingAddress,
        AddressSnapshot billingAddress,
        String notes) {
}
