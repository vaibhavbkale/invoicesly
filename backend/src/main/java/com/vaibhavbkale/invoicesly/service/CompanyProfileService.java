package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.entity.CompanyProfile;

import java.util.List;

public interface CompanyProfileService {
    CompanyProfile create(CompanyProfile profile);
    CompanyProfile update(Long id, CompanyProfile profile);
    void delete(Long id);
    CompanyProfile getById(Long id);
    List<CompanyProfile> getAll();
}
