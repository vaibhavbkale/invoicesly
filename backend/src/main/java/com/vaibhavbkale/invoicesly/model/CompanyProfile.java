package com.vaibhavbkale.invoicesly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "company_profile")
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String phone;
    private String email;
    private String website;

    @Column(name = "gst_no", unique = true, length = 15)
    private String gstNo; // GSTIN (15 characters in India)

    @Column(name = "pan_no", unique = true, length = 10)
    private String panNo; // PAN (10 characters in India)

    @Column(name = "tax_number")
    private String taxNumber; // GST / VAT / PAN etc.

    @Column(name = "invoice_prefix")
    private String invoicePrefix; // "INV-2025-"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters and setters...
}

