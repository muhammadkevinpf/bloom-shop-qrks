package com.bloom.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.bloom.common.event.OrderCreatedEvent.AddressSnapshot;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "order_number", nullable = false, unique = true)
    public String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    public Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItem> items = new ArrayList<>();

    @Column(name = "subtotal_amount", nullable = false)
    public BigDecimal subtotalAmount;

    @Column(name = "shipping_fee", nullable = false)
    public BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false)
    public BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false)
    public BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    public BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    public PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false)
    public AddressSnapshot shippingAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_address")
    public AddressSnapshot billingAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // helper query
    public static Order findByOrderNumber(String orderNumber) {
        return find("orderNumber", orderNumber).firstResult();
    }

    public static List<Order> findByCustomerId(UUID customerId, int pageIndex, int pageSize) {
        return find("customer.id", Sort.descending("createdAt"), customerId)
                .page(pageIndex, pageSize)
                .list();
    }

    public static long countByCustomerId(UUID customerId) {
        return count("customer.id", customerId);
    }

    public static Order findByIdAndCustomerId(UUID id, UUID customerId) {
        return find("id = ?1 and customer.id = ?2", id, customerId).firstResult();
    }

    public static boolean updateStatus(UUID orderId, OrderStatus newStatus) {
        return update("status = ?1, updatedAt = ?2 where id = ?3", newStatus, Instant.now(), orderId) > 0;
    }
}
