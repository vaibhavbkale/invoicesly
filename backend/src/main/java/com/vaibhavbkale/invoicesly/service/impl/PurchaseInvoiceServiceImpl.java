package com.vaibhavbkale.invoicesly.service.impl;

import com.vaibhavbkale.invoicesly.entity.PurchaseInvoice;
import com.vaibhavbkale.invoicesly.exception.ResourceNotFoundException;
import com.vaibhavbkale.invoicesly.repository.PurchaseInvoiceRepository;
import com.vaibhavbkale.invoicesly.service.InvoiceNumberService;
import com.vaibhavbkale.invoicesly.service.PurchaseInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {
    private final PurchaseInvoiceRepository repo;

    @Autowired
    private InvoiceNumberService invoiceNumberService;

    public PurchaseInvoiceServiceImpl(PurchaseInvoiceRepository repo) {
        this.repo = repo;
    }

    @Override
    public PurchaseInvoice create(PurchaseInvoice invoice) {
        String invoiceNo = invoiceNumberService.getNextInvoiceNo("PUR");
        invoice.setInvoiceNumber(invoiceNo);
        return repo.save(invoice);
    }

    @Override
    public PurchaseInvoice update(Long id, PurchaseInvoice invoice) {
        PurchaseInvoice existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found: " + id));
        // copy fields (for brevity copy all important fields; you can expand)
        existing.setInvoiceNumber(invoice.getInvoiceNumber());
        existing.setInvoiceDate(invoice.getInvoiceDate());
        existing.setSupplierFirmOwnerName(invoice.getSupplierFirmOwnerName());
        existing.setSupplierMobile(invoice.getSupplierMobile());
        existing.setSupplierAddress(invoice.getSupplierAddress());
        existing.setCompanyId(invoice.getCompanyId());
        existing.setItems(invoice.getItems()); // will be serialized on save
        existing.setSgst(invoice.getSgst());
        existing.setCgst(invoice.getCgst());
        existing.setWeightShortageAmount(invoice.getWeightShortageAmount());
        existing.setChotariAmount(invoice.getChotariAmount());
        existing.setWeightChargesDeducted(invoice.getWeightChargesDeducted());
        existing.setFinalNetTotal(invoice.getFinalNetTotal());
        existing.setVehicleNumber(invoice.getVehicleNumber());
        existing.setDriverContactNumber(invoice.getDriverContactNumber());
        existing.setDriverName(invoice.getDriverName());
        existing.setAmountInWords(invoice.getAmountInWords());
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public PurchaseInvoice getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found: " + id));
    }

    @Override
    public List<PurchaseInvoice> getAll() {
        return repo.findAll();
    }
}
