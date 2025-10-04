package com.vaibhavbkale.invoicesly.service.impl;

import com.vaibhavbkale.invoicesly.entity.CompanyProfile;
import com.vaibhavbkale.invoicesly.exception.ResourceNotFoundException;
import com.vaibhavbkale.invoicesly.repository.CompanyProfileRepository;
import com.vaibhavbkale.invoicesly.service.CompanyProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {
    private final CompanyProfileRepository repo;

    public CompanyProfileServiceImpl(CompanyProfileRepository repo) {
        this.repo = repo;
    }

    @Override
    public CompanyProfile create(CompanyProfile profile) {
        return repo.save(profile);
    }

    @Override
    public CompanyProfile update(Long id, CompanyProfile profile) {
        CompanyProfile existing = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        existing.setCompanyName(profile.getCompanyName());
        existing.setOwnerName(profile.getOwnerName());
        existing.setMobile(profile.getMobile());
        existing.setAddress(profile.getAddress());
        existing.setGstin(profile.getGstin());
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public CompanyProfile getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    @Override
    public List<CompanyProfile> getAll() {
        return repo.findAll();
    }
}
