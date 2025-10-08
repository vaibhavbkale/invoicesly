package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.entity.SalesInvoice;
import com.vaibhavbkale.invoicesly.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales") // This is the base URL for your sales invoice API
@CrossOrigin(origins = "*") // Allows the HTML file to send requests to this backend
public class SalesController {
    private final SalesInvoiceService service;

    public SalesController(SalesInvoiceService service) {
        this.service = service;
    }

    // The HTML form will send a POST request to this method
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