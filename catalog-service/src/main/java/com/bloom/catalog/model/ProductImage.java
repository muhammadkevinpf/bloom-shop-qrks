package com.bloom.catalog.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
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
@Table(name = "product_images")
public class ProductImage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @JsonIgnore 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @Column(name = "image_url", nullable = false)
    public String imageUrl;

    @Column(name = "alt_text")
    public String altText;

    @Column(name = "is_primary", nullable = false)
    public Boolean isPrimary = false;

    @Column(name = "display_order", nullable = false)
    public int displayOrder = 0;

    // Helper Query
    public static ProductImage findPrimaryByProductId(UUID productId) {
        return find("product.id = ?1 and isPrimary = true", productId).firstResult();
    }

    public static List<ProductImage> findByProductId(UUID productId) {
        return list("product.id", Sort.ascending("displayOrder"), productId);
    }

    public static long deleteByProductId(UUID productId) {
        return delete("product.id", productId);
    }
}
