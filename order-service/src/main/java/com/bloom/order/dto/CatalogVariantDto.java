package com.bloom.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogVariantDto(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        String color,
        String size,
        BigDecimal price) {
}
