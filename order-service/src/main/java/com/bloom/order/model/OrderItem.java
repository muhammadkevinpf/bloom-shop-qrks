package com.bloom.order.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
@Table(name = "order_items")
public class OrderItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    public Order order;

    @Column(name = "variant_id", nullable = false)
    public UUID variantId;

    @Column(name = "product_name", nullable = false)
    public String productName;

    @Column(name = "sku", nullable = false)
    public String sku;

    @Column(name = "color")
    public String color;

    @Column(name = "size")
    public String size;

    @Column(name = "unit_price", nullable = false)
    public BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "total_price", nullable = false)
    public BigDecimal totalPrice;

    public static List<OrderItem> findByOrderId(UUID orderId) {
        return list("order.id", orderId);
    }

}
