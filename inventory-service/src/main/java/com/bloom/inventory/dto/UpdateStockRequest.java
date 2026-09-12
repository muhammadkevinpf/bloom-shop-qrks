package com.bloom.inventory.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
    @NotNull (message = "Variant ID is required") UUID variantId,
    @Min (value = 1, message = "Quantity must be at least 1") int quantity) {}
