package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.model.Product;
import com.vaibhavbkale.invoicesly.repository.ProductRepository;
import com.vaibhavbkale.invoicesly.service.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepo;

    @Autowired
    public ProductController(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product payload) {
        // set defaults if needed
        payload.setActive(payload.getActive() == null ? Boolean.TRUE : payload.getActive());
        // createdAt if you have it
        // payload.setCreatedAt(LocalDateTime.now());
        Product saved = productRepo.save(payload);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(productRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product payload) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        existing.setName(payload.getName());
        existing.setDescription(payload.getDescription());
        existing.setUnitPrice(payload.getUnitPrice());
        existing.setSku(payload.getSku());
        existing.setActive(payload.getActive());
        return ResponseEntity.ok(productRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
