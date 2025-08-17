package com.vaibhavbkale.invoicesly.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateInvoiceRequest {
    public Long customerId;
    public LocalDate invoiceDate;
    public String currency;
    public String notes;
    public List<CreateInvoiceItem> items;
    public CreatePaymentRequest initialPayment; // optional
}
