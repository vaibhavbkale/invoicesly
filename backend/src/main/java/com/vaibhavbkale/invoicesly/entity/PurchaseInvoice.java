package com.vaibhavbkale.invoicesly.entity;

import com.vaibhavbkale.invoicesly.dto.PurchaseItem;
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
@Table(name = "purchase_invoice")
public class PurchaseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Invoice
    private String invoiceNumber;
    private LocalDate invoiceDate;

    // Supplier
    private String supplierFirmOwnerName;
    private String supplierMobile;

    @Column(length = 1000)
    private String supplierAddress;

    // Company
    private Long companyId;

    // Items
    @Lob
    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Transient
    private List<PurchaseItem> items = new ArrayList<>();

    // GST
    private Double sgst;
    private Double cgst;

    // Totals
    private Double weightShortageAmount;
    private Double chotariAmount;
    private Double weightChargesDeducted;
    private Double finalNetTotal;

    // Vehicle
    private String vehicleNumber;
    private String driverContactNumber;
    private String driverName;

    // Amount in Words
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
            throw new RuntimeException("Failed to serialize purchase items", e);
        }
    }

    @PostLoad
    public void readItemsJson() {
        try {
            if (itemsJson != null && !itemsJson.isBlank()) {
                this.items = MAPPER.readValue(itemsJson, new TypeReference<List<PurchaseItem>>() {});
            }
        } catch (IOException e) {
            this.items = new ArrayList<>();
        }
    }
}
