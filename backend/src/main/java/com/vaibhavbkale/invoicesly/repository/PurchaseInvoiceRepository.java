package com.vaibhavbkale.invoicesly.repository;

import com.vaibhavbkale.invoicesly.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> { }
