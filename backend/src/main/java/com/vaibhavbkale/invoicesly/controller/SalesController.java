package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.entity.SalesInvoice;
import com.vaibhavbkale.invoicesly.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesInvoiceService service;

    public SalesController(SalesInvoiceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SalesInvoice> create(@RequestBody SalesInvoice invoice) {
        return ResponseEntity.ok(service.create(invoice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesInvoice> update(@PathVariable Long id, @RequestBody SalesInvoice invoice) {
        return ResponseEntity.ok(service.update(id, invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesInvoice> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SalesInvoice>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
