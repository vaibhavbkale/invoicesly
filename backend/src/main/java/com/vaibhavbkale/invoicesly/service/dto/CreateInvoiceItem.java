package com.vaibhavbkale.invoicesly.service.dto;

import java.math.BigDecimal;

public class CreateInvoiceItem {
    public Long productId; // optional
    public String description;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal taxAmount; // optional per-line tax
}
