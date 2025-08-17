package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.model.Invoice;
import com.vaibhavbkale.invoicesly.model.InvoiceItem;
import com.vaibhavbkale.invoicesly.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/items")
public class InvoiceItemController {

    private final InvoiceRepository invoiceRepo;

    @Autowired
    public InvoiceItemController(InvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceItem>> list(@PathVariable Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        List<InvoiceItem> items = invoice.getItems();
        return ResponseEntity.ok(items == null ? List.of() : items);
    }
}
