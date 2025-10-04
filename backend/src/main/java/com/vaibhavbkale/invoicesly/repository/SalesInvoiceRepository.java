package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> { }
