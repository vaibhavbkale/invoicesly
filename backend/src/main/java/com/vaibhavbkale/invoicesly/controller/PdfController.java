package com.vaibhavbkale.invoicesly.controller;

import com.vaibhavbkale.invoicesly.entity.CompanyProfile;
import com.vaibhavbkale.invoicesly.entity.PurchaseInvoice;
import com.vaibhavbkale.invoicesly.entity.SalesInvoice;
import com.vaibhavbkale.invoicesly.dto.PurchaseItem;
import com.vaibhavbkale.invoicesly.dto.SalesItem;
import com.vaibhavbkale.invoicesly.service.CompanyProfileService;
import com.vaibhavbkale.invoicesly.service.PdfService;
import com.vaibhavbkale.invoicesly.service.PurchaseInvoiceService;
import com.vaibhavbkale.invoicesly.service.SalesInvoiceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;
    private final SalesInvoiceService salesInvoiceService;
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final CompanyProfileService companyProfileService; // optional, used if companyId present

    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public PdfController(PdfService pdfService,
                         SalesInvoiceService salesInvoiceService,
                         PurchaseInvoiceService purchaseInvoiceService,
                         CompanyProfileService companyProfileService) {
        this.pdfService = pdfService;
        this.salesInvoiceService = salesInvoiceService;
        this.purchaseInvoiceService = purchaseInvoiceService;
        this.companyProfileService = companyProfileService;
    }

    @GetMapping("/sales/{id}")
    public ResponseEntity<byte[]> generateSalesPdf(@PathVariable Long id) {
        SalesInvoice invoice = salesInvoiceService.getById(id);
        String html = buildSalesInvoiceHtml(invoice);
        byte[] pdf = pdfService.generatePdfFromHtml(html);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-invoice-" + safeId(id) + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/purchase/{id}")
    public ResponseEntity<byte[]> generatePurchasePdf(@PathVariable Long id) {
        PurchaseInvoice invoice = purchaseInvoiceService.getById(id);
        String html = buildPurchaseInvoiceHtml(invoice);
        byte[] pdf = pdfService.generatePdfFromHtml(html);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-invoice-" + safeId(id) + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ---------------- HTML builders ----------------
    private String buildSalesInvoiceHtml(SalesInvoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader("Sales Invoice"));

        // Header info
        sb.append("<div style='display:flex;justify-content:space-between;'>");
        sb.append("<div>");
        sb.append("<b>Invoice No:</b> ").append(nvl(invoice.getInvoiceNo())).append("<br/>");
        sb.append("<b>Invoice Date:</b> ").append(formatDate(invoice.getInvoiceDate())).append("<br/>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<b>Vehicle No:</b> ").append(nvl(invoice.getVehicleNo())).append("<br/>");
        sb.append("<b>Driver No:</b> ").append(nvl(invoice.getDriverNo())).append("<br/>");
        sb.append("</div>");
        sb.append("</div>");

        // Supplier & Customer snapshot
        sb.append("<hr/>");
        sb.append("<div style='display:flex;justify-content:space-between;'>");
        sb.append("<div style='width:48%'>");
        sb.append("<h4>Supplier</h4>");
        sb.append("<div>").append(nvl(invoice.getSupplierFirmName())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getSupplierContactNo())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getSupplierAddress())).append("</div>");
        sb.append("<div><b>GSTIN:</b> ").append(nvl(invoice.getSupplierGstin())).append("</div>");
        sb.append("</div>");

        sb.append("<div style='width:48%'>");
        sb.append("<h4>Customer</h4>");
        sb.append("<div>").append(nvl(invoice.getCustomerName())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getCustomerContactNo())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getCustomerAddress())).append("</div>");
        sb.append("<div><b>GSTIN:</b> ").append(nvl(invoice.getCustomerGstin())).append("</div>");
        sb.append("</div>");
        sb.append("</div>");

        // Items table
        sb.append("<table class='items'>");
        sb.append("<thead><tr>");
        sb.append("<th>Sr.No.</th><th>Description</th><th>HSN/SAC</th><th>Qty (kg)</th><th>Rate (₹)</th><th>NOS</th><th>Disc %</th><th>Amount (₹)</th>");
        sb.append("</tr></thead><tbody>");

        List<SalesItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            for (SalesItem it : items) {
                sb.append("<tr>");
                sb.append("<td>").append(nvlInt(it.getSrNo())).append("</td>");
                sb.append("<td>").append(nvl(it.getDescription())).append("</td>");
                sb.append("<td>").append(nvl(it.getHsnSac())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getQuantityKg())).append("</td>");
                sb.append("<td>").append(formatMoney(it.getRate())).append("</td>");
                sb.append("<td>").append(nvlInt(it.getNos())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getDiscPercent())).append("</td>");
                sb.append("<td>").append(formatMoney(it.getAmount())).append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='8'>No items</td></tr>");
        }
        sb.append("</tbody></table>");

        // Totals & amounts
        sb.append("<div style='margin-top:10px;text-align:right;'>");
        sb.append("<div><b>Round Off:</b> ₹").append(formatMoney(invoice.getRoundOff())).append("</div>");
        sb.append("<div><b>Total:</b> ₹").append(formatMoney(invoice.getTotal())).append("</div>");
        sb.append("<div><b>Received Amount:</b> ₹").append(formatMoney(invoice.getReceivedAmount())).append("</div>");
        sb.append("<div><b>Pending Amount:</b> ₹").append(formatMoney(invoice.getPendingAmount())).append("</div>");
        sb.append("<div><b>Amount (in words):</b> ").append(nvl(invoice.getAmountInWords())).append("</div>");
        sb.append("</div>");

        sb.append(htmlFooter());
        return sb.toString();
    }

    private String buildPurchaseInvoiceHtml(PurchaseInvoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader("Purchase Invoice"));

        // Date & invoice number if present
        sb.append("<div style='display:flex;justify-content:space-between;'>");
        sb.append("<div>");
        sb.append("<b>Invoice No:</b> ").append(nvl(invoice.getInvoiceNumber())).append("<br/>");
        sb.append("<b>Invoice Date:</b> ").append(formatDate(invoice.getInvoiceDate())).append("<br/>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<b>Vehicle:</b> ").append(nvl(invoice.getVehicleNumber())).append("<br/>");
        sb.append("<b>Driver:</b> ").append(nvl(invoice.getDriverName())).append(" / ").append(nvl(invoice.getDriverContactNumber())).append("<br/>");
        sb.append("</div>");
        sb.append("</div>");

        // Supplier & Self (company) area
        sb.append("<hr/>");
        sb.append("<div style='display:flex;justify-content:space-between;'>");
        sb.append("<div style='width:48%'>");
        sb.append("<h4>Supplier</h4>");
        sb.append("<div>").append(nvl(invoice.getSupplierFirmOwnerName())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getSupplierMobile())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getSupplierAddress())).append("</div>");
        sb.append("</div>");

        sb.append("<div style='width:48%'>");
        sb.append("<h4>Self Details</h4>");
        // if companyId is present, try to fetch CompanyProfile and display; else just show companyId
        if (invoice.getCompanyId() != null) {
            try {
                CompanyProfile cp = companyProfileService.getById(invoice.getCompanyId());
                sb.append("<div>").append(nvl(cp.getCompanyName())).append("</div>");
                sb.append("<div>").append(nvl(cp.getOwnerName())).append("</div>");
                sb.append("<div>").append(nvl(cp.getMobile())).append("</div>");
                sb.append("<div>").append(nvl(cp.getAddress())).append("</div>");
            } catch (Exception ex) {
                sb.append("<div>Company ID: ").append(invoice.getCompanyId()).append("</div>");
            }
        } else {
            sb.append("<div>No company snapshot (companyId null)</div>");
        }
        sb.append("</div>");
        sb.append("</div>");

        // Items table for purchase invoice
        sb.append("<table class='items'>");
        sb.append("<thead><tr>");
        sb.append("<th>Item</th><th>Weight (kg)</th><th>Price/kg (₹)</th><th>GST%</th><th>Shortage (kg)</th><th>Chotari %</th><th>Weight Charges (₹)</th>");
        sb.append("</tr></thead><tbody>");

        List<PurchaseItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            for (PurchaseItem it : items) {
                sb.append("<tr>");
                sb.append("<td>").append(nvl(it.getItemName())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getWeightKg())).append("</td>");
                sb.append("<td>").append(formatMoney(it.getPricePerKg())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getGstPercent())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getShortageKg())).append("</td>");
                sb.append("<td>").append(formatNumber(it.getChotariPercent())).append("</td>");
                sb.append("<td>").append(formatMoney(it.getWeightCharges())).append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='7'>No items</td></tr>");
        }
        sb.append("</tbody></table>");

        // GST & totals
        sb.append("<div style='margin-top:10px;text-align:right;'>");
        sb.append("<div><b>SGST:</b> ").append(formatNumber(invoice.getSgst())).append("%</div>");
        sb.append("<div><b>CGST:</b> ").append(formatNumber(invoice.getCgst())).append("%</div>");
        sb.append("<div><b>Weight Shortage Amount:</b> ₹").append(formatMoney(invoice.getWeightShortageAmount())).append("</div>");
        sb.append("<div><b>Chotari Amount:</b> ₹").append(formatMoney(invoice.getChotariAmount())).append("</div>");
        sb.append("<div><b>Weight Charges Deducted:</b> ₹").append(formatMoney(invoice.getWeightChargesDeducted())).append("</div>");
        sb.append("<div style='font-size:1.1em;'><b>Final Net Total:</b> ₹").append(formatMoney(invoice.getFinalNetTotal())).append("</div>");
        sb.append("</div>");

        sb.append(htmlFooter());
        return sb.toString();
    }

    // ---------------- small helpers ----------------
    private String htmlHeader(String title) {
        return "<html><head>"
                + "<style>"
                + "body{font-family:Arial, sans-serif; font-size:12px;}"
                + ".items{width:100%; border-collapse:collapse; margin-top:10px;}"
                + ".items th, .items td{border:1px solid #444; padding:6px;}"
                + ".items th{background:#efefef;}"
                + "</style></head><body><h2 style='text-align:center;'>" + title + "</h2>";
    }

    private String htmlFooter() {
        return "<div style='margin-top:25px;'>"
                + "<div style='float:left; width:50%'><p>Prepared by: ____________________</p></div>"
                + "<div style='float:right; width:50%; text-align:right;'><p>Authorised Signatory</p></div>"
                + "<div style='clear:both;'></div>"
                + "</div></body></html>";
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String nvlInt(Number n) {
        return n == null ? "" : String.valueOf(n);
    }

    private String formatMoney(Number n) {
        if (n == null) return "0.00";
        return moneyFmt.format(n.doubleValue());
    }

    private String formatNumber(Number n) {
        if (n == null) return "";
        return moneyFmt.format(n.doubleValue());
    }

    private String formatDate(java.time.LocalDate date) {
        return date == null ? "" : date.format(dateFmt);
    }

    private String safeId(Long id) {
        return id == null ? "0" : String.valueOf(id);
    }
}
