package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.entity.CompanyProfile;
import com.vaibhavbkale.invoicesly.service.CompanyProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*") // <-- Allow frontend Axios calls from any domain
public class CompanyController {
    private final CompanyProfileService service;

    public CompanyController(CompanyProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CompanyProfile> create(@RequestBody CompanyProfile profile) {
        return ResponseEntity.ok(service.create(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyProfile> update(@PathVariable Long id, @RequestBody CompanyProfile profile) {
        return ResponseEntity.ok(service.update(id, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyProfile> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CompanyProfile>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
