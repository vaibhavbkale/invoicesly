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
        sb.append(htmlHeader("Bill of Supply"));

        // Use a single major table for the entire invoice structure
        sb.append("<table class='main-invoice'>");

        // ROW 1: SUPPLIER DETAILS (LEFT) and BASIC INVOICE INFO (RIGHT)
        sb.append("<tr>");
        sb.append("<td style='width:50%; padding:0; border-right:none;'>"); // Left Half Container (Supplier/Consignee/Buyer)

        // --- SUPPLIER SECTION ---
        sb.append("<table style='width:100%; border-collapse:collapse;'>");
        sb.append("<tr><td style='border:1px solid #444; border-top:none; border-left:none; padding:5px; height:18px;'>");
        sb.append("<div style='font-size:1.1em; font-weight:bold;'>").append(nvl(invoice.getSupplierFirmName())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getSupplierAddress())).append("</div>");
        sb.append("<div><b>GSTIN/UIN:</b> ").append(nvl(invoice.getSupplierGstin())).append("</div>");
        sb.append("<div>State Name: Maharashtra, Code: 27</div>");
        sb.append("<div>E-Mail: ").append(nvl(invoice.getSupplierContactNo())).append("</div>");
        sb.append("</td></tr>");

        // --- CONSIGNEE SECTION ---
        sb.append("<tr><td style='border:1px solid #444; border-left:none; padding:5px; height:18px;'>");
        sb.append("<div style='font-weight:bold; padding-bottom:3px;'>Consignee (Ship to)</div>");
        sb.append("<div style='font-weight:bold;'>").append(nvl(invoice.getCustomerName())).append("</div>");
        sb.append("<div>").append(nvl(invoice.getCustomerAddress())).append("</div>");
        sb.append("<div><b>GSTIN/UIN:</b> ").append(nvl(invoice.getCustomerGstin())).append("</div>");
        sb.append("<div>State Name: Maharashtra, Code: 27</div>");
        sb.append("</td></tr>");

        // --- BUYER SECTION ---
        sb.append("<tr><td style='border:1px solid #444; border-left:none; border-bottom:none; padding:5px; height:18px;'>");
        sb.append("<div style='font-weight:bold; padding-bottom:3px;'>Buyer (Bill to)</div>");
        sb.append("<div style='font-weight:bold;'>").append(nvl(invoice.getCustomerName())).append("</div>"); // Reusing Customer for Buyer
        sb.append("<div>").append(nvl(invoice.getCustomerAddress())).append("</div>");
        sb.append("<div><b>GSTIN/UIN:</b> ").append(nvl(invoice.getCustomerGstin())).append("</div>");
        sb.append("<div>State Name: Maharashtra, Code: 27</div>");
        sb.append("</td></tr>");

        sb.append("</table>");
        sb.append("</td>"); // End of Left Half Container


        sb.append("<td style='width:50%; padding:0; border-left:none; border-right:none;'>"); // Right Half Container (Info & Dispatch)
        sb.append("<table style='width:100%; border-collapse:collapse;'>");

        // --- INVOICE INFO GRID (3 ROWS, 6 COLUMNS) ---
        sb.append("<tr><td colspan='6' style='padding:0;'>");
        sb.append("<table style='width:100%; border-collapse:collapse;'>");

        // Row 1: Invoice No, E-Way Bill No, Dated
        sb.append("<tr>");
        sb.append("<td class='info-label' style='width:20%;'>Invoice No.</td><td class='info-value' style='width:20%;'>").append(nvl(invoice.getInvoiceNo())).append("</td>");
        sb.append("<td class='info-label' style='width:20%;'>e-Way Bill No.</td><td class='info-value' style='width:20%;'></td>");
        sb.append("<td class='info-label' style='width:10%;'>Dated</td><td class='info-value' style='width:10%;'>").append(formatDate(invoice.getInvoiceDate())).append("</td>");
        sb.append("</tr>");

        // Row 2: Delivery Note, Payment Terms, Other References
        sb.append("<tr>");
        sb.append("<td class='info-label'>Delivery Note</td><td class='info-value'></td>");
        sb.append("<td class='info-label'>Mode/Terms of Payment</td><td class='info-value'></td>");
        sb.append("<td class='info-label' colspan='2' style='border-left:1px solid #444;'>Other References</td>");
        sb.append("</tr>");

        // Row 3: Reference No & Date, Buyer's Order No & Date
        sb.append("<tr>");
        sb.append("<td class='info-label'>Reference No. &amp; Date.</td><td class='info-value'></td>");
        sb.append("<td class='info-label'>Buyer's Order No.</td><td class='info-value'></td>");
        sb.append("<td class='info-label' style='border-left:1px solid #444;'>Dated</td><td class='info-value'></td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</td></tr>");

        // --- DISPATCH INFO GRID (3 ROWS, 6 COLUMNS - RESTORED) ---
        sb.append("<tr><td colspan='6' style='padding:0;'>");
        sb.append("<table style='width:100%; border-collapse:collapse; border-top:none;'>");

        // Row 1: Dispatch Doc No & Date, Delivery Note Date
        sb.append("<tr>");
        sb.append("<td class='info-label' style='width:20%;'>Dispatch Doc No.</td><td class='info-value' style='width:20%;'>").append(nvl(invoice.getInvoiceNo())).append("</td>");
        sb.append("<td class='info-label' style='width:20%;'>Delivery Note Date</td><td class='info-value' style='width:20%;'></td>");
        sb.append("<td class='info-label' style='width:10%;'></td><td class='info-value' style='width:10%;'></td>"); // Empty cell for alignment
        sb.append("</tr>");

        // Row 2: Dispatched through, Destination, Motor Vehicle No.
        sb.append("<tr>");
        sb.append("<td class='info-label'>Dispatched through</td><td class='info-value'></td>");
        sb.append("<td class='info-label' colspan='2'>Destination</td>");
        sb.append("<td class='info-label' colspan='2'>Motor Vehicle No.</td>");
        sb.append("</tr>");

        // Row 3: Bill of Lading, Terms of Delivery, Vehicle Number Value
        sb.append("<tr>");
        sb.append("<td class='info-label'>Bill of Lading/LR-RR No.</td><td class='info-value'></td>");
        sb.append("<td class='info-label' colspan='2'>Terms of Delivery</td>");
        sb.append("<td class='info-value' colspan='2' style='text-align:center;'>").append(nvl(invoice.getVehicleNo())).append("</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</td></tr>");

        sb.append("</table>");
        sb.append("</td>"); // End of Right Half Container
        sb.append("</tr>"); // End of ROW 1

        // ROW 2: ITEMS TABLE HEADER (Merged across the full width)
        sb.append("<tr><td colspan='2' style='padding:0; border-top:1px solid #444; border-left:none; border-right:none;'>");

        // Items table (Item details)
        sb.append("<table class='items'>");
        sb.append("<thead><tr>");
        sb.append("<th style='width:5%'>Sl No.</th><th style='width:35%'>Description of Goods</th><th style='width:10%'>HSN/SAC</th><th style='width:10%'>Quantity</th><th style='width:10%'>Rate (₹)</th><th style='width:5%'>Disc %</th><th style='width:20%'>Amount (₹)</th>");
        sb.append("</tr></thead><tbody>");

        List<SalesItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            for (SalesItem it : items) {
                sb.append("<tr>");
                sb.append("<td class='center'>").append(nvlInt(it.getSrNo())).append("</td>");
                // Escape any potential ampersands in user input for XML safety
                sb.append("<td>").append(nvl(it.getDescription()).replace("&", "&amp;")).append("</td>");
                sb.append("<td class='center'>").append(nvl(it.getHsnSac())).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getQuantityKg())).append(" kg</td>");
                sb.append("<td class='right'>").append(formatMoney(it.getRate())).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getDiscPercent())).append("</td>");
                sb.append("<td class='right'>").append(formatMoney(it.getAmount())).append("</td>");
                sb.append("</tr>");
            }
        } else {
            // Placeholder row for alignment if no items are present
            sb.append("<tr>");
            sb.append("<td class='center'></td>");
            sb.append("<td></td>");
            sb.append("<td class='center'>17011410</td>");
            sb.append("<td class='right'></td>");
            sb.append("<td class='right'></td>");
            sb.append("<td class='right'></td>");
            sb.append("<td class='right'></td>");
            sb.append("</tr>");
        }

        // Total Row (Item Quantity Total)
        double totalQuantity = items != null ? items.stream().mapToDouble(i -> i.getQuantityKg() != null ? i.getQuantityKg().doubleValue() : 0.0).sum() : 0.0;

        // Empty space rows for visual padding (matching image layout)
        for(int i = 0; i < 2; i++) {
            sb.append("<tr><td colspan='7' style='padding: 3px;'></td></tr>");
        }

        // EXPENSES/ROUND OFF/TOTAL ROW
        sb.append("<tr>");
        sb.append("<td colspan='3' style='border-left:none; border-bottom:none; border-top:none; text-align:left; font-weight:bold; padding-left:10px;'></td>");
        sb.append("<td class='right'><b>").append(formatNumber(totalQuantity)).append(" kg</b></td>"); // Qty Total
        sb.append("<td colspan='3' style='padding:0; border-right:none; border-bottom:none; border-top:none;'>");

        sb.append("<table style='width:100%; border-collapse:collapse; font-size:11px;'>");
        sb.append("<tr><td class='info-label' style='width:35%; border-right:1px solid #444;'>EXPENSES</td><td class='info-value' style='width:65%;'>").append(formatNumber(invoice.getPendingAmount())).append("</td></tr>");
        sb.append("<tr><td class='info-label' style='width:35%; border-right:1px solid #444;'>Round Off</td><td class='info-value' style='width:65%;'>").append(formatMoney(invoice.getRoundOff())).append("</td></tr>");
        // Final Total Row
        sb.append("<tr><td class='info-label' style='width:35%; border-right:1px solid #444; background:#efefef; font-size:1.1em;'>Total</td><td class='info-value' style='width:65%; background:#efefef; font-size:1.1em; font-weight:bold;'>₹ ").append(formatMoney(invoice.getTotal())).append("</td></tr>");
        sb.append("</table>");

        sb.append("</td></tr>");
        sb.append("</tbody></table>");
        sb.append("</td></tr>"); // End of Items table cell

        // ROW 3: FOOTER SECTION (Amount in words, HSN, Tax, Signatory)
        sb.append("<tr><td colspan='2' style='padding:0; border-top:1px solid #444; border-left:none; border-right:none;'>");
        sb.append("<table style='width:100%; border-collapse:collapse;'>");

        // Top row: Amount in words
        sb.append("<tr>");
        sb.append("<td style='width:70%; border-left:none; border-right:1px solid #444; padding:5px; height:18px;'>");
        sb.append("<b>Amount Chargeable (in words)</b>");
        sb.append("<div style='font-size:1.1em; font-weight:bold; margin-top:5px;'>INR ").append(nvl(invoice.getAmountInWords())).append("</div>");
        sb.append("</td>");
        // Escaping the raw '&' in 'E & O E'
        sb.append("<td style='width:30%; border-right:none; padding:5px; height:18px; text-align:right;'>E &amp; O E</td>");
        sb.append("</tr>");

        // HSN/TAX SUMMARY GRID (Simplified)
        sb.append("<tr>");
        sb.append("<td colspan='2' style='padding:0; border-left:none; border-right:none; border-top:1px solid #444;'>");
        sb.append("<table class='tax-summary'>");
        sb.append("<thead><tr>");
        sb.append("<th style='width:70%; border-left:none;'>HSN/SAC</th><th style='width:30%; border-right:none;'>Taxable Value</th>");
        sb.append("</tr></thead><tbody>");

        // HSN row (Using first item's HSN or default)
        String hsnSac = items != null && !items.isEmpty() ? nvl(items.get(0).getHsnSac()) : "17011410"; // Default HSN if no items
        sb.append("<tr>");
        sb.append("<td class='center' style='border-left:none;'>").append(hsnSac).append("</td>");
        sb.append("<td class='right' style='border-right:none;'>").append(formatMoney(invoice.getTotal())).append("</td>"); // Reusing Total as Taxable Value
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td class='center' style='border-left:none; font-weight:bold;'>Total</td>");
        sb.append("<td class='right' style='border-right:none; font-weight:bold;'>").append(formatMoney(invoice.getTotal())).append("</td>");
        sb.append("</tr>");

        sb.append("</tbody></table>");
        sb.append("</td></tr>");

        // TAX IN WORDS & DECLARATION & SIGNATORY
        sb.append("<tr>");
        sb.append("<td style='width:70%; border-left:none; border-right:1px solid #444; border-bottom:none; padding:5px; height:18px;'>");
        // Tax Amount in Words (Always NIL)
        sb.append("<b>Tax Amount (in words):</b> NIL");
        sb.append("<div style='margin-top:10px;'><b>Declaration:</b> We declare that this invoice shows the actual price of the goods described.</div>");
        sb.append("</td>");

        sb.append("<td style='width:30%; border-right:none; border-bottom:none; padding:5px; height:18px; text-align:right;'>");
        sb.append("<div style='padding-top:20px; font-weight:bold;'>for ").append(nvl(invoice.getSupplierFirmName())).append("</div>");
        sb.append("<div style='margin-top:30px; border-top:1px solid #444; padding-top:5px;'>Authorised Signatory</div>");
        sb.append("</td></tr>");

        sb.append("</table>");
        sb.append("</td></tr>"); // End of Footer Section

        sb.append("</table>"); // Close main-invoice table

        sb.append("<div style='text-align:center; font-size:10px; margin-top:5px;'>This is a Computer Generated Invoice</div>");

        sb.append(htmlFooter());
        return sb.toString();
    }

    private String buildPurchaseInvoiceHtml(PurchaseInvoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader("Purchase Invoice"));

        // Date & invoice number if present
        sb.append("<div style='display:flex;justify-content:space-between;border:1px solid #444;border-bottom:none;padding:5px;'>");
        sb.append("<div>");
        sb.append("<b>Invoice No:</b> ").append(nvl(invoice.getInvoiceNumber())).append("<br/>");
        sb.append("<b>Invoice Date:</b> ").append(formatDate(invoice.getInvoiceDate())).append("<br/>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<b>Vehicle:</b> ").append(nvl(invoice.getVehicleNumber())).append("<br/>");
        // Escape & in driver/contact number just in case
        sb.append("<b>Driver:</b> ").append(nvl(invoice.getDriverName()).replace("&", "&amp;")).append(" / ").append(nvl(invoice.getDriverContactNumber())).append("<br/>");
        sb.append("</div>");
        sb.append("</div>");

        // Supplier & Self (company) area - Using table for better structure (similar to Sales)
        sb.append("<table class='parties'>");
        sb.append("<tr><td colspan='2' class='title'><h4>Supplier (Billed From)</h4></td><td colspan='2' class='title'><h4>Self Details (Billed To)</h4></td></tr>");

        // Row 1: Names/Company
        sb.append("<tr>");
        sb.append("<td style='width:15%;'>Name:</td><td style='width:35%;'>").append(nvl(invoice.getSupplierFirmOwnerName()).replace("&", "&amp;")).append("</td>");
        sb.append("<td style='width:15%;'>Company:</td><td style='width:35%;'>");

        if (invoice.getCompanyId() != null) {
            try {
                CompanyProfile cp = companyProfileService.getById(invoice.getCompanyId());
                sb.append(nvl(cp.getCompanyName()).replace("&", "&amp;"));
            } catch (Exception ex) {
                sb.append("Company ID: ").append(invoice.getCompanyId());
            }
        } else {
            sb.append("No company snapshot (companyId null)");
        }
        sb.append("</td></tr>");

        // Row 2: Contact
        sb.append("<tr>");
        sb.append("<td>Contact:</td><td>").append(nvl(invoice.getSupplierMobile())).append("</td>");
        sb.append("<td>Owner:</td><td>");
        if (invoice.getCompanyId() != null) {
            try {
                CompanyProfile cp = companyProfileService.getById(invoice.getCompanyId());
                sb.append(nvl(cp.getOwnerName()).replace("&", "&amp;")).append(" / ").append(nvl(cp.getMobile()));
            } catch (Exception ex) {
                sb.append("");
            }
        }
        sb.append("</td></tr>");

        // Row 3: Address
        sb.append("<tr>");
        // Escape any potential ampersands in user input for XML safety
        sb.append("<td>Address:</td><td>").append(nvl(invoice.getSupplierAddress()).replace("&", "&amp;")).append("</td>");
        sb.append("<td>Address:</td><td>");
        if (invoice.getCompanyId() != null) {
            try {
                CompanyProfile cp = companyProfileService.getById(invoice.getCompanyId());
                sb.append(nvl(cp.getAddress()).replace("&", "&amp;"));
            } catch (Exception ex) {
                sb.append("");
            }
        }
        sb.append("</td></tr>");
        sb.append("</table>");

        // Items table for purchase invoice
        sb.append("<table class='items' style='margin-top:0;'>");
        sb.append("<thead><tr>");
        sb.append("<th>Item</th><th>Weight (kg)</th><th>Price/kg (₹)</th><th>GST%</th><th>Shortage (kg)</th><th>Chotari %</th><th>Weight Charges (₹)</th>");
        sb.append("</tr></thead><tbody>");

        List<PurchaseItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            for (PurchaseItem it : items) {
                sb.append("<tr>");
                sb.append("<td>").append(nvl(it.getItemName()).replace("&", "&amp;")).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getWeightKg())).append("</td>");
                sb.append("<td class='right'>").append(formatMoney(it.getPricePerKg())).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getGstPercent())).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getShortageKg())).append("</td>");
                sb.append("<td class='right'>").append(formatNumber(it.getChotariPercent())).append("</td>");
                sb.append("<td class='right'>").append(formatMoney(it.getWeightCharges())).append("</td>");
                sb.append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='7' class='center'>No items</td></tr>");
        }
        sb.append("</tbody></table>");

        // GST & totals - Using table for structured layout
        sb.append("<table class='items' style='width:100%; border-top: none;'>");
        sb.append("<tr>");
        sb.append("<td style='width:70%; border-right:none; border-bottom:none;'></td>"); // Empty space
        sb.append("<td style='width:30%; border-left:none;'>"); // Totals column

        sb.append("<div style='text-align:right;padding:5px;'>");
        sb.append("<div><b>SGST:</b> ").append(formatNumber(invoice.getSgst())).append("%</div>");
        sb.append("<div><b>CGST:</b> ").append(formatNumber(invoice.getCgst())).append("%</div>");
        sb.append("<div><b>Weight Shortage Amount:</b> ₹").append(formatMoney(invoice.getWeightShortageAmount())).append("</div>");
        sb.append("<div><b>Chotari Amount:</b> ₹").append(formatMoney(invoice.getChotariAmount())).append("</div>");
        sb.append("<div><b>Weight Charges Deducted:</b> ₹").append(formatMoney(invoice.getWeightChargesDeducted())).append("</div>");
        sb.append("<div style='font-size:1.1em;'><b>Final Net Total:</b> ₹").append(formatMoney(invoice.getFinalNetTotal())).append("</div>");
        sb.append("</div>");

        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append(htmlFooter());
        return sb.toString();
    }

    // ---------------- small helpers ----------------
    private String htmlHeader(String title) {
        return "<html><head>"
                + "<style>"
                + "body{font-family:Arial, sans-serif; font-size:11px;}"
                + "h2{margin-top:0;margin-bottom:5px;}"

                // General table styles for the new layout
                + ".main-invoice{width:100%; border-collapse:collapse; border:1px solid #444;}"
                + ".main-invoice td{border:1px solid #444; padding:0; vertical-align:top; height:18px;}"
                + ".main-invoice h4{margin:0;}"

                // Nested table styles for info grids
                + ".info-label {border-right:1px solid #444; padding:5px; height:18px; font-weight:bold;}"
                + ".info-value {padding:5px; height:18px;}"

                // Specific table for the item list
                + ".items{width:100%; border-collapse:collapse; border:none !important;}"
                + ".items th, .items td{border:1px solid #444; padding:6px;}"
                + ".items th{background:#efefef; text-align:center;}"
                + ".items td.right{text-align:right;}"
                + ".items td.center{text-align:center;}"

                // Specific table for tax summary
                + ".tax-summary{width:100%; border-collapse:collapse;}"
                + ".tax-summary th, .tax-summary td{border:1px solid #444; padding:6px;}"
                + ".tax-summary th{background:#efefef; text-align:center;}"
                + ".tax-summary td.right{text-align:right;}"
                + ".tax-summary td.center{text-align:center;}"

                // Party table for purchase invoice
                + ".parties{width:100%; border-collapse:collapse;}"
                + ".parties tr:last-child td { border-bottom: 1px solid #444; }"
                + ".parties td{border:1px solid #444; padding:4px; vertical-align:top;}"
                + ".parties td.title{background:#efefef; text-align:center; padding:8px 4px;}"

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