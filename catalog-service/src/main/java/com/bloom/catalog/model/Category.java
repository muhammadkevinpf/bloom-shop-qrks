package com.bloom.catalog.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity 
@Table (name = "categories")
public class Category extends PanacheEntityBase {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    public Long id;
    
    @NotBlank (message = "Category name is required")
    @Size (max = 100, message = "Category name must not exceed 100 characters")
    @Column (name = "name", nullable = false, length = 100)
    public String name;

    @NotBlank (message = "Slug is required")
    @Size (max = 120)
    @Column (name = "slug", nullable = false, length = 120, unique = true)
    public String slug;

    @Column (name = "description", columnDefinition = "TEXT")
    public String description;

    // Self-referencing relationship for subcategories (parent_id)
    @JsonIgnore 
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "parent_id")
    public Category parent;

    // Children subcategories
    @OneToMany (mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Category> children = new ArrayList<>();

    @Column (name = "image_url", length = 500)
    public String imageUrl;

    @Column (name = "is_active", nullable = false)
    public Boolean isActive = true;

    @CreationTimestamp 
    @Column (name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    // Helper Query
    public static Category findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }

    public static List<Category> findRootCategories() {
        return list("parent is null and isActive = true");
    }

    public static List<Category> findActiveCategories() {
        return list("isActive", true);
    }
}
