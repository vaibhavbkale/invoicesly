package com.vaibhavbkale.invoicesly.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreatePaymentRequest {
    public BigDecimal amount;
    public LocalDateTime paymentDate; // if null, service will set now
    public String method;
    public String txnReference;
}
