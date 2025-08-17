package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.service.dto.CreateInvoiceRequest;
import com.vaibhavbkale.invoicesly.service.dto.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoice(Long id);
    Page<InvoiceResponse> listInvoices(Pageable pageable);
    byte[] generateInvoicePdf(Long id) throws Exception;
}
