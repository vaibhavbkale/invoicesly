package com.vaibhavbkale.invoicesly.service.impl;

import com.vaibhavbkale.invoicesly.entity.SalesInvoice;
import com.vaibhavbkale.invoicesly.exception.ResourceNotFoundException;
import com.vaibhavbkale.invoicesly.repository.SalesInvoiceRepository;
import com.vaibhavbkale.invoicesly.service.InvoiceNumberService;
import com.vaibhavbkale.invoicesly.service.SalesInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesInvoiceServiceImpl implements SalesInvoiceService {
    private final SalesInvoiceRepository repo;

    @Autowired
    private InvoiceNumberService invoiceNumberService;

    public SalesInvoiceServiceImpl(SalesInvoiceRepository repo) {
        this.repo = repo;
    }

    @Override
    public SalesInvoice create(SalesInvoice invoice) {
        String invoiceNo = invoiceNumberService.getNextInvoiceNo("SAL");
        invoice.setInvoiceNo(invoiceNo);
        return repo.save(invoice);
    }

    @Override
    public SalesInvoice update(Long id, SalesInvoice invoice) {
        SalesInvoice existing = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found: " + id));
        existing.setInvoiceNo(invoice.getInvoiceNo());
        existing.setInvoiceDate(invoice.getInvoiceDate());
        existing.setVehicleNo(invoice.getVehicleNo());
        existing.setDriverNo(invoice.getDriverNo());
        existing.setSupplierFirmName(invoice.getSupplierFirmName());
        existing.setSupplierContactNo(invoice.getSupplierContactNo());
        existing.setSupplierAddress(invoice.getSupplierAddress());
        existing.setSupplierGstin(invoice.getSupplierGstin());
        existing.setCustomerName(invoice.getCustomerName());
        existing.setCustomerContactNo(invoice.getCustomerContactNo());
        existing.setCustomerAddress(invoice.getCustomerAddress());
        existing.setCustomerGstin(invoice.getCustomerGstin());
        existing.setItems(invoice.getItems());
        existing.setRoundOff(invoice.getRoundOff());
        existing.setTotal(invoice.getTotal());
        existing.setReceivedAmount(invoice.getReceivedAmount());
        existing.setPendingAmount(invoice.getPendingAmount());
        existing.setAmountInWords(invoice.getAmountInWords());
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public SalesInvoice getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found: " + id));
    }

    @Override
    public List<SalesInvoice> getAll() {
        return repo.findAll();
    }
}
