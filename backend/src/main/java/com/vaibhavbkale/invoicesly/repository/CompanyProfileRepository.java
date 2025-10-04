package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> { }