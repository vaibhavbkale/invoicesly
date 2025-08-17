package com.vaibhavbkale.invoicesly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customers",
        indexes = {@Index(name = "idx_customer_name", columnList = "name")})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactName;
    private String email;
    private String phone;
    private String address;
    @Column(name = "tax_id")
    private String taxId; // GST/VAT etc.

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters and setters...
}

