package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.model.Invoice;

public interface PdfService {
    byte[] generateInvoicePdf(Invoice invoice) throws Exception;
}
