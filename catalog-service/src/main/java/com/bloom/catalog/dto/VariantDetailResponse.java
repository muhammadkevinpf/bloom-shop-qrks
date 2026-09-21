package com.bloom.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.bloom.catalog.model.ProductVariant;

public record VariantDetailResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        String color,
        String size,
        BigDecimal price) {

    public static VariantDetailResponse from(ProductVariant variant) {
        BigDecimal finalPrice = variant.product.basePrice.add(variant.priceAdjustment);
        return new VariantDetailResponse(variant.id, variant.product.id, variant.product.name, variant.sku,
                variant.color, variant.size, finalPrice);
    }

}
