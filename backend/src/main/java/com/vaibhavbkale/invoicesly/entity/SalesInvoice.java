package com.vaibhavbkale.invoicesly.entity;

import com.vaibhavbkale.invoicesly.dto.SalesItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sales_invoice")
public class SalesInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNo;
    private LocalDate invoiceDate;
    private String vehicleNo;
    private String driverNo;

    // Supplier snapshot
    private String supplierFirmName;
    private String supplierContactNo;
    @Column(length = 1000)
    private String supplierAddress;
    private String supplierGstin;

    // Customer snapshot
    private String customerName;
    private String customerContactNo;
    @Column(length = 1000)
    private String customerAddress;
    private String customerGstin;

    // items stored as JSON
    @Lob
    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Transient
    private List<SalesItem> items = new ArrayList<>();

    private Double roundOff;
    private Double total;
    private Double receivedAmount;
    private Double pendingAmount;
    @Column(length = 1000)
    private String amountInWords;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PrePersist
    @PreUpdate
    public void writeItemsJson() {
        try {
            if (items != null) {
                this.itemsJson = MAPPER.writeValueAsString(items);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize sales items", e);
        }
    }

    @PostLoad
    public void readItemsJson() {
        try {
            if (itemsJson != null && !itemsJson.isBlank()) {
                this.items = MAPPER.readValue(itemsJson, new TypeReference<List<SalesItem>>() {
                });
            }
        } catch (IOException e) {
            this.items = new ArrayList<>();
        }
    }
}
