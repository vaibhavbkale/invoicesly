package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.entity.InvoiceSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, String> {
}
