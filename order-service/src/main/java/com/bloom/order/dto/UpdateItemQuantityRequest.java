package com.bloom.order.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateItemQuantityRequest(
        @NotNull(message = "Item ID is required") UUID itemId,
        @Min(value = 0, message = "Quantity must be at least 0") int newQuantity) {

}
