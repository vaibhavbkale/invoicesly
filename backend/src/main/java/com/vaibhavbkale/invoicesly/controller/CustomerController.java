package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.model.Customer;
import com.vaibhavbkale.invoicesly.repository.CustomerRepository;
import com.vaibhavbkale.invoicesly.service.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepo;

    @Autowired
    public CustomerController(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer payload) {
        payload.setCreatedAt(LocalDateTime.now());
        Customer saved = customerRepo.save(payload);
        return ResponseEntity.created(URI.create("/api/customers/" + saved.getId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> list() {
        return ResponseEntity.ok(customerRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        Customer c = customerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        return ResponseEntity.ok(c);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer payload) {
        Customer existing = customerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        existing.setName(payload.getName());
        existing.setContactName(payload.getContactName());
        existing.setEmail(payload.getEmail());
        existing.setPhone(payload.getPhone());
        existing.setAddress(payload.getAddress());
        existing.setTaxId(payload.getTaxId());
        existing.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(customerRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Customer existing = customerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        customerRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
