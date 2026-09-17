package com.bloom.order.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name="customers")
public class Customer extends PanacheEntityBase {
    
    @Id 
    @Column (name = "id")
    public UUID id;

    @Column (name = "email", nullable = false, unique = true)
    public String email;

    @Column (name = "first_name")
    public String firstName;

    @Column (name = "last_name")
    public String lastName;

    @Column (name = "phone_number")
    public String phoneNumber;

    @CreationTimestamp 
    @Column (name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp 
    @Column (name = "updated_at", nullable = false)
    public Instant updatedAt;

    // Query Helper 
    public static Customer findById(UUID id) {
        return find("id", id).firstResult();
    }
    
    public static Customer findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static Customer findOrCreate(UUID id, String email, String firstName, String lastName, String phoneNumber) {
        Customer customer = Customer.findById(id);
        if (customer == null) {
            customer = new Customer();
            customer.id = id;
            customer.email = email;
            customer.firstName = firstName;
            customer.lastName = lastName;
            customer.phoneNumber = phoneNumber;
            customer.persist();
        }

        return customer;
    }

    public static boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public static Customer findByPhoneNumber(String phoneNumber) {
        return find("phoneNumber", phoneNumber).firstResult();
    }
}
