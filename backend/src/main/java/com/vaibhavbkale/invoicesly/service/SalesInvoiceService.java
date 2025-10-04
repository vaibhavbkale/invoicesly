package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.entity.SalesInvoice;

import java.util.List;

public interface SalesInvoiceService {
    SalesInvoice create(SalesInvoice invoice);
    SalesInvoice update(Long id, SalesInvoice invoice);
    void delete(Long id);
    SalesInvoice getById(Long id);
    List<SalesInvoice> getAll();
}
