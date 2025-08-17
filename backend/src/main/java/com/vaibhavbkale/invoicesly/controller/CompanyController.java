package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.model.CompanyProfile;
import com.vaibhavbkale.invoicesly.repository.CompanyProfileRepository;
import com.vaibhavbkale.invoicesly.service.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyProfileRepository companyRepo;

    @Autowired
    public CompanyController(CompanyProfileRepository companyRepo) {
        this.companyRepo = companyRepo;
    }

    @PostMapping
    public ResponseEntity<CompanyProfile> create(@RequestBody CompanyProfile payload) {
        payload.setCreatedAt(LocalDateTime.now());
        CompanyProfile saved = companyRepo.save(payload);
        return ResponseEntity.created(URI.create("/api/companies/" + saved.getId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CompanyProfile>> list() {
        return ResponseEntity.ok(companyRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyProfile> get(@PathVariable Long id) {
        CompanyProfile cp = companyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        return ResponseEntity.ok(cp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyProfile> update(@PathVariable Long id, @RequestBody CompanyProfile payload) {
        CompanyProfile existing = companyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        existing.setName(payload.getName());
        existing.setAddress(payload.getAddress());
        existing.setEmail(payload.getEmail());
        existing.setPhone(payload.getPhone());
        existing.setGstNo(payload.getGstNo());
        existing.setPanNo(payload.getPanNo());
        existing.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(companyRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        CompanyProfile existing = companyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        companyRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
