package com.bloom.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bloom.catalog.model.Category;
import com.bloom.catalog.model.Product;
import com.bloom.catalog.model.ProductImage;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        String status,
        Boolean featured,
        String imageUrl,
        CategorySummary category,
        Instant createdAt) {

    public record CategorySummary(
            Long id,
            String name,
            String slug) {
        public static CategorySummary from(Category category) {
            return new CategorySummary(category.id, category.name, category.slug);
        }
    }

    public static ProductSummaryResponse from(Product product, ProductImage productImage) {
        String primaryUrl = productImage != null ? productImage.imageUrl : null;
        return new ProductSummaryResponse(
                product.id,
                product.name,
                product.slug,
                product.description,
                product.basePrice,
                product.status,
                product.featured,
                primaryUrl,
                CategorySummary.from(product.category),
                product.createdAt);
    }

    public static List<ProductSummaryResponse> fromList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }
        return products.stream()
                .map(p -> from(p, ProductImage.findPrimaryByProductId(p.id)))
                .toList();
    }
}