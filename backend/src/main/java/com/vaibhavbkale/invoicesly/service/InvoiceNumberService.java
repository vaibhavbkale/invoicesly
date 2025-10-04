package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.entity.InvoiceSequence;
import com.vaibhavbkale.invoicesly.repository.InvoiceSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class InvoiceNumberService {

    private final InvoiceSequenceRepository repo;

    public InvoiceNumberService(InvoiceSequenceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public String getNextInvoiceNo(String typePrefix) {
        String yearPrefix = LocalDate.now().getYear() + ""; // 2025
        String fullPrefix = typePrefix + "-" + yearPrefix;  // SAL-2025

        InvoiceSequence seq = repo.findById(fullPrefix).orElseGet(() -> {
            InvoiceSequence s = new InvoiceSequence();
            s.setPrefix(fullPrefix);
            s.setLastNumber(0L);
            return s;
        });

        seq.setLastNumber(seq.getLastNumber() + 1);
        repo.save(seq);

        return fullPrefix + "-" + String.format("%05d", seq.getLastNumber());
    }
}
