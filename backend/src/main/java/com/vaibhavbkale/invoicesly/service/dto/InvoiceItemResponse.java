package com.vaibhavbkale.invoicesly.service.dto;

import java.math.BigDecimal;

public class InvoiceItemResponse {
    public Long id;
    public String description;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal lineTotal;
    public BigDecimal taxAmount;
}
