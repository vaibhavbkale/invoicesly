package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.model.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
}
