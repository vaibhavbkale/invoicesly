package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.entity.PurchaseInvoice;
import com.vaibhavbkale.invoicesly.service.PurchaseInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {
    private final PurchaseInvoiceService service;

    public PurchaseController(PurchaseInvoiceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PurchaseInvoice> create(@RequestBody PurchaseInvoice invoice) {
        return ResponseEntity.ok(service.create(invoice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseInvoice> update(@PathVariable Long id, @RequestBody PurchaseInvoice invoice) {
        return ResponseEntity.ok(service.update(id, invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoice> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseInvoice>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
