package com.vaibhavbkale.invoicesly.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoices",
        indexes = {@Index(name = "idx_invoice_number", columnList = "invoice_number")})
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber; // human readable

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxTotal;

    @Column(name = "total", precision = 15, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "INR";

    @Column(length = 50)
    private String status; // DRAFT, UNPAID, PARTIALLY_PAID, PAID, CANCELLED

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Payment> payments;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version; // optimistic locking

    // getters and setters...
}

