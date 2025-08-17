package com.vaibhavbkale.invoicesly.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceResponse {
    public Long id;
    public String invoiceNumber;
    public LocalDate invoiceDate;
    public Long customerId;
    public BigDecimal subtotal;
    public BigDecimal taxTotal;
    public BigDecimal total;
    public String status;
    public List<InvoiceItemResponse> items;
}
