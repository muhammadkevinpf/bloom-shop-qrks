package com.bloom.catalog.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
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
@Table(name = "products")
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    public Category category;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "slug", nullable = false)
    public String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "base_price", nullable = false)
    public BigDecimal basePrice;

    @Column(name = "status", nullable = false)
    public String status = "ACTIVE";

    @Column(name = "featured", nullable = false)
    public Boolean featured = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // Helper Query
    public static Product findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }

    public static List<Product> findFeatured() {
        return list("featured = true and status = 'ACTIVE'");
    }

    public static List<Product> findLatest(int limit) {
        return find("status = 'ACTIVE'", Sort.descending("createdAt"))
                .page(Page.of(0, limit))
                .list();
    }

    public static List<Product> findByCategoryId(Long categoryId, int pageIndex, int pageSize) {
        return find("categoryId = ?1 and status = 'ACTIVE'", Sort.descending("createdAt"), categoryId)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public static List<Product> search(String keyword) {
        String query = "%" + keyword.toLowerCase() + "%";
        return list("status = 'ACTIVE' and (lower(name) like ?1 or lower(description) like ?1)", query);
    }

    public static boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }
}
