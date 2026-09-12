package com.bloom.inventory.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory")
public class Inventory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "variant_id", nullable = false, unique = true)
    public UUID variantId;

    @Column(name = "available_stock", nullable = false)
    public int availableStock = 0;

    @Column(name = "reserved_stock", nullable = false)
    public int reservedStock = 0;

    @Version 
    @Column(name = "version", nullable = false)
    public long version;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // Helper Query
    public static Inventory findByVariantId(UUID variantId) {
        return find("variantId", variantId).firstResult();
    }

    public static boolean checkAvailibility(UUID variantId, int requestedQuantity) {
        Inventory inventory = findByVariantId(variantId);
        return inventory != null && inventory.availableStock >= requestedQuantity;
    }

    public static List<Inventory> findLowStock(int threshold) {
        return list("availableStock <= ?1", threshold);
    }
}
