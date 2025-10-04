package com.vaibhavbkale.invoicesly.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequence {
    @Id
    private String prefix;   // e.g., "SAL-2025" or "PUR-2025"
    private Long lastNumber; // e.g., 101
}
