package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.entity.PurchaseInvoice;

import java.util.List;

public interface PurchaseInvoiceService {
    PurchaseInvoice create(PurchaseInvoice invoice);
    PurchaseInvoice update(Long id, PurchaseInvoice invoice);
    void delete(Long id);
    PurchaseInvoice getById(Long id);
    List<PurchaseInvoice> getAll();
}
