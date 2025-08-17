package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.model.*;
import com.vaibhavbkale.invoicesly.repository.*;
import com.vaibhavbkale.invoicesly.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final PdfService pdfService;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              CustomerRepository customerRepository,
                              ProductRepository productRepository,
                              PaymentRepository paymentRepository,
                              PdfService pdfService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.pdfService = pdfService;
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // fetch customer
        Customer customer = customerRepository.findById(request.customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId));

        // create invoice entity
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(request.invoiceDate == null ? LocalDate.now() : request.invoiceDate);
        invoice.setCurrency(request.currency == null ? "INR" : request.currency);
        invoice.setNotes(request.notes);
        invoice.setStatus("UNPAID");
        invoice.setCreatedAt(LocalDateTime.now());

        // calculate items
        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (CreateInvoiceItem inItem : Optional.ofNullable(request.items).orElse(Collections.emptyList())) {
            InvoiceItem item = new InvoiceItem();
            item.setProduct(inItem.productId == null ? null :
                    productRepository.findById(inItem.productId).orElse(null));
            item.setDescription(inItem.description);
            int qty = (inItem.quantity == null || inItem.quantity <= 0) ? 1 : inItem.quantity;
            item.setQuantity(qty);
            BigDecimal unitPrice = inItem.unitPrice == null ? BigDecimal.ZERO : inItem.unitPrice;
            item.setUnitPrice(unitPrice);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            item.setLineTotal(lineTotal);
            BigDecimal taxAmt = inItem.taxAmount == null ? BigDecimal.ZERO : inItem.taxAmount;
            item.setTaxAmount(taxAmt);

            subtotal = subtotal.add(lineTotal);
            taxTotal = taxTotal.add(taxAmt);

            items.add(item);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setTotal(subtotal.add(taxTotal));

        // Save invoice first so we get ID for numbering
        Invoice saved = invoiceRepository.save(invoice);

        // generate invoice number based on DB ID
        String invoiceNumber = "INV-" + String.format("%06d", saved.getId());
        saved.setInvoiceNumber(invoiceNumber);

        items.forEach(i -> i.setInvoice(saved));
        saved.setItems(items);
        invoiceRepository.save(saved);  // no reassignment


        // if initial payment provided
        if (request.initialPayment != null && request.initialPayment.amount != null
                && request.initialPayment.amount.compareTo(BigDecimal.ZERO) > 0) {
            CreatePaymentRequest p = request.initialPayment;
            Payment payment = new Payment();
            payment.setInvoice(saved);
            payment.setAmount(p.amount);
            payment.setPaymentDate(p.paymentDate == null ? LocalDateTime.now() : p.paymentDate);
            payment.setMethod(p.method);
            payment.setTxnReference(p.txnReference);
            paymentRepository.save(payment);

            // recalc paid amount
            BigDecimal paidSoFar = paymentRepository.findByInvoiceId(saved.getId())
                    .stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));  // explicit add

            if (paidSoFar.compareTo(saved.getTotal()) >= 0) {
                saved.setStatus("PAID");
            } else {
                saved.setStatus("PARTIALLY_PAID");
            }
            invoiceRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long id) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return mapToResponse(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listInvoices(Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findAll(pageable);
        List<InvoiceResponse> dtoList = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long id) throws Exception {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return pdfService.generateInvoicePdf(inv);
    }

    // ---- helper methods ----
    private InvoiceResponse mapToResponse(Invoice invoice) {
        InvoiceResponse r = new InvoiceResponse();
        r.id = invoice.getId();
        r.invoiceNumber = invoice.getInvoiceNumber();
        r.invoiceDate = invoice.getInvoiceDate();
        r.customerId = invoice.getCustomer() == null ? null : invoice.getCustomer().getId();
        r.subtotal = invoice.getSubtotal();
        r.taxTotal = invoice.getTaxTotal();
        r.total = invoice.getTotal();
        r.status = invoice.getStatus();
        if (invoice.getItems() != null) {
            r.items = invoice.getItems().stream().map(it -> {
                InvoiceItemResponse ir = new InvoiceItemResponse();
                ir.id = it.getId();
                ir.description = it.getDescription();
                ir.quantity = it.getQuantity();
                ir.unitPrice = it.getUnitPrice();
                ir.lineTotal = it.getLineTotal();
                ir.taxAmount = it.getTaxAmount();
                return ir;
            }).collect(Collectors.toList());
        }
        return r;
    }
}
