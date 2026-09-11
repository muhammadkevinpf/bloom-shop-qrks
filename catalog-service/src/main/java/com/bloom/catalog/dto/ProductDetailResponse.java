package com.bloom.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bloom.catalog.model.Category;
import com.bloom.catalog.model.Product;
import com.bloom.catalog.model.ProductImage;
import com.bloom.catalog.model.ProductVariant;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        String status,
        Boolean featured,
        Category category,
        List<ProductVariantDto> variants,
        List<ProductImageDto> images,
        Instant createdAt) {

    public record ProductVariantDto(
            UUID id,
            String sku,
            String color,
            String size,
            BigDecimal priceAdjustment) {

        public static ProductVariantDto from(ProductVariant v) {
            return new ProductVariantDto(v.id, v.sku, v.color, v.size, v.priceAdjustment);
        }
    }

    public record ProductImageDto(
            UUID id,
            String imageUrl,
            String altText,
            Boolean isPrimary,
            int displayOrder) {

        public static ProductImageDto from(ProductImage img) {
            return new ProductImageDto(img.id, img.imageUrl, img.altText, img.isPrimary, img.displayOrder);
        }
    }

    public static ProductDetailResponse of(Product product, List<ProductVariant> variants, List<ProductImage> images) {
        return new ProductDetailResponse(
                product.id,
                product.name,
                product.slug,
                product.description,
                product.basePrice,
                product.status,
                product.featured,
                product.category,
                variants.stream().map(ProductVariantDto::from).toList(),
                images.stream().map(ProductImageDto::from).toList(),
                product.createdAt);
    }
}