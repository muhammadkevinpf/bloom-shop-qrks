package com.bloom.order.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "cart_items")
public class CartItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @JoinColumn(name = "customer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    public Customer customer;

    @Column(name = "variant_id", nullable = false)
    public UUID variantId;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // helper query
    public static List<CartItem> findByCustomerId(UUID customerId) {
        return list("customer.id", customerId);
    }

    public static CartItem findByCustomerAndVariant(UUID customerId, UUID variantId) {
        return find("customer.id = ?1 and variantId = ?2", customerId, variantId).firstResult();
    }

    public static CartItem addOrIncrement(Customer customer, UUID variantId, int quantity) {
        CartItem existing = findByCustomerAndVariant(customer.id, variantId);
        if (existing != null) {
            existing.quantity += quantity;
            return existing;
        } else {
            CartItem newItem = new CartItem();
            newItem.customer = customer;
            newItem.variantId = variantId;
            newItem.quantity = quantity;
            newItem.persist();
            return newItem;
        }
    }

    public static boolean deleteByCustomerId(UUID customerId) {
        return delete("customer.id", customerId) > 0;
    }

    public static boolean deleteByIdAndCustomerId(UUID id, UUID customerId) {
        return delete("id = ?1 and customer.id = ?2", id, customerId) > 0;
    }

    public static boolean updateQuantity(UUID itemId, UUID customerId, int newQuantity) {
        if (newQuantity <= 0) {
            return deleteByIdAndCustomerId(itemId, customerId);
        }

        return update("quantity = ?1 where id = ?2 and customer.id = ?3", newQuantity, itemId, customerId) > 0;
    }
}
