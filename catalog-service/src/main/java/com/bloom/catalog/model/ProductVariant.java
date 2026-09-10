package com.bloom.catalog.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variants")
public class ProductVariant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @JsonIgnore 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @Column(name = "sku", nullable = false, unique = true)
    public String sku;

    @Column(name = "color")
    public String color;

    @Column(name = "size")
    public String size;

    @Column(name = "price_adjustment", nullable = false)
    public BigDecimal priceAdjustment = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    // Helper Query
    public static ProductVariant findBySku(String sku) {
        return find("sku", sku).firstResult();
    }

    public static List<ProductVariant> findByProductId(UUID productId) {
        return list("product.id", productId);
    }

    public static boolean existsBySku(String sku) {
        return count("sku", sku) > 0;
    }

    public static ProductVariant findByProductAndOptions(UUID productId, String color, String size) {
        return find("product.id = ?1 and color = ?2 and size = ?3", productId, color, size).firstResult();
    }
}
