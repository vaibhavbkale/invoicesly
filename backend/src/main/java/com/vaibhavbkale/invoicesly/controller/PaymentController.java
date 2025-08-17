package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.model.Invoice;
import com.vaibhavbkale.invoicesly.model.Payment;
import com.vaibhavbkale.invoicesly.repository.InvoiceRepository;
import com.vaibhavbkale.invoicesly.repository.PaymentRepository;
import com.vaibhavbkale.invoicesly.service.ResourceNotFoundException;
import com.vaibhavbkale.invoicesly.service.dto.CreatePaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/payments")
public class PaymentController {

    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;

    @Autowired
    public PaymentController(InvoiceRepository invoiceRepo, PaymentRepository paymentRepo) {
        this.invoiceRepo = invoiceRepo;
        this.paymentRepo = paymentRepo;
    }

    @PostMapping
    public ResponseEntity<Payment> create(@PathVariable Long invoiceId, @RequestBody CreatePaymentRequest req) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        Payment p = new Payment();
        p.setInvoice(invoice);
        p.setAmount(req.amount);
        p.setPaymentDate(req.paymentDate == null ? LocalDateTime.now() : req.paymentDate);
        p.setMethod(req.method);
        p.setTxnReference(req.txnReference);

        Payment saved = paymentRepo.save(p);
        return ResponseEntity.created(URI.create("/api/invoices/" + invoiceId + "/payments/" + saved.getId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> list(@PathVariable Long invoiceId) {
        // uses PaymentRepository.findByInvoiceId(...)
        List<Payment> payments = paymentRepo.findByInvoiceId(invoiceId);
        return ResponseEntity.ok(payments);
    }
}
