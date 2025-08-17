package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.service.InvoiceService;
import com.vaibhavbkale.invoicesly.service.ResourceNotFoundException;
import com.vaibhavbkale.invoicesly.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@RequestBody CreateInvoiceRequest request) {
        InvoiceResponse created = invoiceService.createInvoice(request);
        return ResponseEntity.created(URI.create("/api/invoices/" + created.id)).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable Long id) {
        InvoiceResponse r = invoiceService.getInvoice(id);
        return ResponseEntity.ok(r);
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "invoiceDate,desc") String sort
    ) {
        Sort sortObj = Sort.by(Sort.Order.desc("invoiceDate"));
        // basic parsing if client passed e.g. invoiceDate,asc
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortObj = Sort.by("asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]);
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<InvoiceResponse> pageResult = invoiceService.listInvoices(pageable);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        try {
            byte[] pdf = invoiceService.generateInvoicePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("invoice_" + id + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (ResourceNotFoundException rnfe) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
