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
@Table(name = "customer_addresses")
public class CustomerAddress extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @JsonIgnore 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(name = "label")
    public String label = "Home";

    @Column(name = "recipient_name", nullable = false)
    public String recipientName;

    @Column(name = "phone_number", nullable = false)
    public String phoneNumber;

    @Column(name = "street_line1", nullable = false)
    public String streetLine1;

    @Column(name = "street_line2")
    public String streetLine2;

    @Column(name = "city", nullable = false)
    public String city;

    @Column(name = "state_province", nullable = false)
    public String stateProvince;

    @Column(name = "postal_code", nullable = false)
    public String postalCode;

    @Column(name = "country_code", nullable = false)
    public String countryCode = "ID";

    @Column(name = "is_default_shipping", nullable = false)
    public boolean isDefaultShipping = false;

    @Column(name = "is_default_billing", nullable = false)
    public boolean isDefaultBilling = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    // helper query
    public static List<CustomerAddress> findByCustomerId(UUID customerId) {
        return list("customer.id", customerId);
    }

    public static CustomerAddress findDefaultShipping(UUID customerId) {
        return find("customer.id = ?1 and isDefaultShipping = true", customerId).firstResult();
    }

    public static CustomerAddress findDefaultBilling(UUID customerId) {
        return find("customer.id = ?1 and isDefaultBilling = true", customerId).firstResult();
    }

    public static boolean setDefaultShipping(UUID addressId, UUID customerId) {
        update("isDefaultShipping = false where customer.id = ?1", customerId);
        return update("isDefaultShipping = true where id = ?1 and customer.id = ?2", addressId, customerId) > 0;
    }

    public static boolean setDefaultBilling(UUID addressId, UUID customerId) {
        update("isDefaultBilling = false where customer.id = ?1", customerId);
        return update("isDefaultBilling = true where id = ?1 and customer.id = ?2", addressId, customerId) > 0;
    }

    public static boolean deleteByIdAndCustomerId(UUID id, UUID customerId) {
        return delete("id = ?1 and customer.id = ?2", id, customerId) > 0;
    }
}
