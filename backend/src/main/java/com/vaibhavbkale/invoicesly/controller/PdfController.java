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
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=sales-invoice-" + safeId(id) + ".pdf")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdf);
        }

        @GetMapping("/purchase/{id}")
        public ResponseEntity<byte[]> generatePurchasePdf(@PathVariable Long id) {
                PurchaseInvoice invoice = purchaseInvoiceService.getById(id);
                String html = buildPurchaseInvoiceHtml(invoice);
                byte[] pdf = pdfService.generatePdfFromHtml(html);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=purchase-invoice-" + safeId(id) + ".pdf")
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
                sb.append("<td style='width:50%; padding:0; border-right:none;'>"); // Left Half Container
                                                                                    // (Supplier/Consignee/Buyer)

                // --- SUPPLIER SECTION ---
                sb.append("<table style='width:100%; border-collapse:collapse;'>");
                sb.append(
                                "<tr><td style='border:1px solid #444; border-top:none; border-left:none; padding:5px; height:18px;'>");
                sb.append("<div style='font-size:1.1em; font-weight:bold;'>").append(nvl(invoice.getSupplierFirmName()))
                                .append("</div>");
                sb.append("<div>").append(nvl(invoice.getSupplierAddress())).append("</div>");
                sb.append("<div>GSTIN/UIN: ").append(nvl(invoice.getSupplierGstin())).append("</div>");
                sb.append("<div>State Name: Maharashtra, Code: 27</div>");
                sb.append("<div>E-Mail: ").append(nvl(invoice.getSupplierContactNo())).append("</div>");
                sb.append("</td></tr>");

                // --- CONSIGNEE SECTION ---
                sb.append("<tr><td style='border:1px solid #444; border-left:none; padding:5px; height:18px;'>");
                sb.append("<div style='padding-bottom:3px;'>Consignee (Ship to)</div>");
                sb.append("<div style='font-weight:bold;'>").append(nvl(invoice.getCustomerName())).append("</div>");
                sb.append("<div>").append(nvl(invoice.getCustomerAddress())).append("</div>");
                sb.append("<div>GSTIN/UIN: ").append(nvl(invoice.getCustomerGstin())).append("</div>");
                sb.append("<div>State Name: Maharashtra, Code: 27</div>");
                sb.append("</td></tr>");

                // --- BUYER SECTION ---
                sb.append(
                                "<tr><td style='border:1px solid #444; border-left:none; border-bottom:none; padding:5px; height:18px;'>");
                sb.append("<div style=' padding-bottom:3px;'>Buyer (Bill to)</div>");
                sb.append("<div style='font-weight:bold;'>").append(nvl(invoice.getCustomerName())).append("</div>"); // Reusing
                                                                                                                      // Customer
                                                                                                                      // for
                                                                                                                      // Buyer
                sb.append("<div>").append(nvl(invoice.getCustomerAddress())).append("</div>");
                sb.append("<div>GSTIN/UIN:").append(nvl(invoice.getCustomerGstin())).append("</div>");
                sb.append("<div>State Name: Maharashtra, Code: 27</div>");
                sb.append("</td></tr>");

                sb.append("</table>");
                sb.append("</td>"); // End of Left Half Container
                // RIGHT HALF CONTAINER (Info & Dispatch)
                sb.append("<td style='width:50%; padding:0; border-left:none; border-right:none;'>");
                sb.append("<table style='width:100%; border-collapse:collapse; font-size:11px;'>");

                // === INVOICE INFO & DISPATCH SECTION ===
                String boldStyle = "style='font-weight:bold; font-size:11px;'";
                String cellStyle = "style='border:1px solid #444; vertical-align:top; padding:4px;'";

                // Row 1: Invoice No. | e-Way Bill No.
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Invoice No.<br/><span " + boldStyle + ">")
                                .append(nvl(invoice.getInvoiceNo())).append("</span></td>");
                sb.append("<td ").append(cellStyle).append(">e-Way Bill No.<br/><span " + boldStyle + ">")
                                .append("20250864984").append("</span></td>"); // Hardcoded or make dynamic if field
                                                                               // exists
                sb.append("</tr>");

                // Row 2: Dated | Delivery Note
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Dated<br/><span " + boldStyle + ">")
                                .append(formatDate(invoice.getInvoiceDate())).append("</span></td>");
                sb.append("<td ").append(cellStyle).append(">Delivery Note<br/></td>");
                sb.append("</tr>");

                // Row 3: Mode/Terms of Payment | Other References
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Mode/Terms of Payment<br/></td>");
                sb.append("<td ").append(cellStyle).append(">Other References<br/></td>");
                sb.append("</tr>");

                // Row 4: Reference No. & Date | Buyer's Order No. & Date
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Reference No. &amp; Date<br/></td>");
                sb.append("<td ").append(cellStyle).append(">Buyer's Order No. &amp; Date<br/></td>");
                sb.append("</tr>");

                // Row 5: Dispatch Doc No. | Delivery Note Date
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Dispatch Doc No.<br/><span " + boldStyle + ">")
                                .append(nvl(invoice.getInvoiceNo())).append("</span></td>");
                sb.append("<td ").append(cellStyle).append(">Delivery Note Date<br/></td>");
                sb.append("</tr>");

                // Row 6: Dispatched through | Destination
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Dispatched through<br/><span " + boldStyle + ">")
                                .append("8010038041").append("</span></td>"); // Hardcoded or dynamic if available
                sb.append("<td ").append(cellStyle).append(">Destination<br/></td>");
                sb.append("</tr>");

                // Row 7: Bill of Lading/LR-RR No. | Motor Vehicle No.
                sb.append("<tr>");
                sb.append("<td ").append(cellStyle).append(">Bill of Lading/LR-RR No.<br/></td>");
                sb.append("<td ").append(cellStyle).append(">Motor Vehicle No.<br/><span " + boldStyle + ">")
                                .append(nvl(invoice.getVehicleNo())).append("</span></td>");
                sb.append("</tr>");

                // Row 8: Terms of Delivery (Single cell)
                sb.append("<tr>");
                sb.append("<td colspan='2' ").append(cellStyle).append(">Terms of Delivery<br/><br/>")
                                .append("<span " + boldStyle + ">TRANSPORT CHARGESS = 20900</span></td>");
                sb.append("</tr>");

                sb.append("</table>");
                sb.append("</td>"); // End of Right Half
                sb.append("</tr>");

                // --- ROW 2: ITEMS TABLE HEADER (REPLACEMENT START) ---
                sb.append("<tr><td colspan='2' style='padding:0; border-top:1px solid #444; border-left:none; border-right:none;'>");

                // Items Table (with GST% and PER columns) - widths tuned to match ref image
                sb.append("<table class='items' style='width:100%; border-collapse:collapse; border:0;'>");
                sb.append("<thead>");
                sb.append("<tr>");
                sb.append("<th style='width:4%; border:1px solid #444; padding:6px; background:#f6f6f6;'>Sl No.</th>");
                sb.append("<th style='width:28%; text-align:left; border:1px solid #444; padding:6px; background:#f6f6f6;'>Description of Goods</th>");
                sb.append("<th style='width:10%; border:1px solid #444; padding:6px; background:#f6f6f6;'>HSN/SAC</th>");
                sb.append("<th style='width:9%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:center;'>GST Rate</th>");
                sb.append("<th style='width:10%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:right;'>Quantity</th>");
                sb.append("<th style='width:8%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:right;'>Rate</th>");
                sb.append("<th style='width:6%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:center;'>Per</th>");
                sb.append("<th style='width:7%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:center;'>Disc. %</th>");
                sb.append("<th style='width:8%; border:1px solid #444; padding:6px; background:#f6f6f6; text-align:right;'>Amount</th>");
                sb.append("</tr>");
                sb.append("</thead>");
                sb.append("<tbody>");

                // items loop (keeps your existing logic)
                List<SalesItem> items = invoice.getItems();
                if (items != null && !items.isEmpty()) {
                        for (SalesItem it : items) {
                                sb.append("<tr style='vertical-align:top;'>");

                                // Sl No.
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:center;'>")
                                                .append(nvlInt(it.getSrNo()))
                                                .append("</td>");

                                // Description column - include EXPENSES block below main description (left
                                // area)
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:left;'>");
                                // main description (escape &)
                                sb.append("<div style='font-weight:bold;'>")
                                                .append(nvl(it.getDescription()).replace("&", "&amp;"))
                                                .append("</div>");
                                // small vertical spacing
                                sb.append("<div style='height:10px;'></div>");
                                // EXPENSES / TRANSPORT ADVANCE / Less : Round Up lines (static placement like
                                // image)
                                sb.append("<div style='margin-top:10px; font-style:italic; font-weight:bold; text-align:left;'>")
                                                .append("EXPENSES")
                                                .append("</div>");
                                sb.append("<div style='font-weight:bold; margin-top:4px; text-align:left;'>TRANSPORT ADVANCE</div>");
                                sb.append("<div style='margin-top:6px;'><span style='font-weight:bold;'>Less :</span> <span style='font-weight:bold;'>Round Up</span></div>");
                                sb.append("</td>");

                                // HSN/SAC
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:center;'>")
                                                .append(nvl(it.getHsnSac()))
                                                .append("</td>");

                                // GST Rate (STATIC as requested; change to it.getGstRate() if available)
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:center;'>")
                                                .append("0 %")
                                                .append("</td>");

                                // Quantity (right aligned, bold)
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:right; font-weight:bold;'>")
                                                .append(formatNumber(it.getQuantityKg()))
                                                .append(" KG</td>");

                                // Rate
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:right;'>")
                                                .append(formatMoney(it.getRate()))
                                                .append("</td>");

                                // Per (static KG)
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:center;'>KG</td>");

                                // Disc %
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:center;'>")
                                                .append(formatNumber(it.getDiscPercent()))
                                                .append("</td>");

                                // Amount - this cell will also contain expense values for the same row in
                                // stacked fashion (right-most column)
                                sb.append("<td style='border-left:1px solid #444; border-right:1px solid #444; padding:8px; text-align:right; font-weight:bold;'>");
                                sb.append(formatMoney(it.getAmount())); // main amount

                                // If you want expense values (transport advance / round up) to appear under
                                // amount, append them here.
                                // Using invoice fields for the example — adapt to your actual expense fields if
                                // they exist.
                                // We show them as stacked values on the right (like reference image).
                                if (invoice.getPendingAmount() != null
                                                && invoice.getPendingAmount().doubleValue() != 0.0) {
                                        sb.append("<div style='font-weight:normal; margin-top:8px;'>")
                                                        .append(formatMoney(invoice.getPendingAmount()))
                                                        .append("</div>");
                                }
                                // Round off display (example: invoice.getRoundOff())
                                if (invoice.getRoundOff() != null && invoice.getRoundOff().doubleValue() != 0.0) {
                                        sb.append("<div style='font-weight:normal; margin-top:4px;'>")
                                                        .append(formatMoney(invoice.getRoundOff()))
                                                        .append("</div>");
                                }
                                sb.append("</td>");

                                sb.append("</tr>");
                        }
                } else {
                        // placeholder row
                        sb.append("<tr>");
                        sb.append("<td style='border:1px solid #444; padding:8px;' colspan='9' class='center'>No items available</td>");
                        sb.append("</tr>");
                }

                // TOTAL / ROUND OFF SECTION placed exactly under Description column and Amount
                // as requested
                double totalQuantity = items != null
                                ? items.stream().mapToDouble(
                                                i -> i.getQuantityKg() != null ? i.getQuantityKg().doubleValue() : 0.0)
                                                .sum()
                                : 0.0;

                sb.append("<tr>");
                sb.append("<td style='border:1px solid #444; padding:6px; text-align:left; font-weight:bold;' colspan='2'>Total</td>"); // spans
                                                                                                                                        // Sl
                                                                                                                                        // No
                                                                                                                                        // +
                                                                                                                                        // Description
                                                                                                                                        // visually
                                                                                                                                        // like
                                                                                                                                        // image
                sb.append("<td style='border:1px solid #444; padding:6px;'></td>"); // HSN empty
                sb.append("<td style='border:1px solid #444; padding:6px; text-align:right; font-weight:bold;'></td>"); // GST
                                                                                                                        // rate
                                                                                                                        // empty
                sb.append("<td style='border:1px solid #444; padding:6px; text-align:right; font-weight:bold;'>")
                                .append(formatNumber(totalQuantity)).append(" KG")
                                .append("</td>"); // quantity total
                sb.append("<td style='border:1px solid #444; padding:6px;'></td>"); // rate empty
                sb.append("<td style='border:1px solid #444; padding:6px;'></td>"); // per empty
                sb.append("<td style='border:1px solid #444; padding:6px;'></td>"); // disc empty
                sb.append("<td style='border:1px solid #444; padding:6px; text-align:right; font-weight:bold;'>₹ ")
                                .append(formatMoney(invoice.getTotal()))
                                .append("</td>");
                sb.append("</tr>");

                sb.append("</tbody>");
                sb.append("</table>"); // end items table
                sb.append("</td></tr>");
                // --- ROW 2: ITEMS TABLE HEADER (REPLACEMENT END) ---

                // --- FOOTER SECTION (Amount in words, Declaration, Bank Details, Authorized
                // Signatory) ---
                sb.append("<tr>");
                sb.append("<td colspan='2' style='padding:0; border-top:1px solid #444; border-left:none; border-right:none;'>");
                sb.append("<table style='width:100%; border-collapse:collapse;'>");

                // LEFT SIDE (Amount in words + Declaration) as a boxed area
                sb.append("<tr>");
                sb.append("<td style='width:65%; vertical-align:top; padding:10px; border-right:1px solid #fff;'>"); // use
                                                                                                                     // white
                                                                                                                     // right
                                                                                                                     // border
                                                                                                                     // so
                                                                                                                     // visually
                                                                                                                     // no
                                                                                                                     // border
                                                                                                                     // for
                                                                                                                     // bank
                                                                                                                     // panel
                sb.append("<div style='font-weight:bold;'>Amount Chargeable (in words)</div>");
                sb.append("<div style='font-size:1.05em; font-weight:bold; margin-top:6px;'>INR ")
                                .append(nvl(invoice.getAmountInWords()).replace("&", "&amp;"))
                                .append("</div>");
                sb.append("<div style='margin-top:20px; font-size:11px; line-height:1.4;'>");
                sb.append("<b>Declaration</b>");
                sb.append("<div style='margin-top:6px;'>We declare that this invoice shows the actual price of the goods described and that all particulars are true and correct.</div>");
                sb.append("</div>");
                sb.append("</td>");

                // RIGHT SIDE (Bank details + Authorized Signatory) — no outer box border as
                // requested
                sb.append("<td style='width:35%; vertical-align:top; padding:10px;'>");
                sb.append("<div style='text-align:left; font-weight:bold; margin-bottom:6px;'>Company's Bank Details</div>");
                sb.append("<table style='width:100%; border-collapse:collapse; font-size:11px; border:none;'>");
                sb.append("<tr><td style='width:40%; border:none;'>A/c Holder's Name :</td><td style='font-weight:bold; border:none;'>CHAURANG AGRO FOODS</td></tr>");
                sb.append("<tr><td style='border:none;'>Bank Name :</td><td style='border:none;'>HDFC BANK</td></tr>");
                sb.append("<tr><td style='border:none;'>A/c No. :</td><td style='border:none;'>50200052160348</td></tr>");
                sb.append("<tr><td style='border:none;'>Branch &amp; IFS Code :</td><td style='border:none;'>URULI KACHAN &amp; HDFC0002988</td></tr>");
                sb.append("</table>");

                // authorized signatory block aligned to right
                sb.append("<div style='margin-top:18px; text-align:right; font-weight:bold;'>for ")
                                .append(nvl(invoice.getSupplierFirmName()).replace("&", "&amp;"))
                                .append("</div>");
                sb.append("<div style='margin-top:22px; border-top:1px solid #444; padding-top:6px; text-align:right;'>Authorised Signatory</div>");

                sb.append("</td>");
                sb.append("</tr>");

                sb.append("</table>");
                sb.append("</td></tr>");

                // small footer text

                sb.append("</table>");
                sb.append("<tr><td colspan='2' style='padding:6px; text-align:center; font-size:10px;'>This is a Computer Generated Invoice</td></tr>");
                // sb.append(htmlFooter());

                // PAGE BREAK — start E-Way Bill on new page
                sb.append("<div style='page-break-before: always;'></div>");
                // Start of E-Way Bill Page
                sb.append("<div style='font-family:Arial, sans-serif; font-size:12px; padding:10px;'>");
                sb.append("<h3 style='text-align:center; margin-bottom:8px;'>e-Way Bill</h3>");

                sb.append("<div>");
                sb.append("<b>Doc No:</b> ").append(nvl(invoice.getInvoiceNo())).append("<br/>");
                sb.append("<b> Date:</b> ").append(formatDate(invoice.getInvoiceDate())).append("<br/>");
                sb.append("</div>");
                sb.append("<hr style='border:0.5px solid #444; margin-top:20px;'/>");
                // Section 1 - Vehicle Info
                sb.append("<div style='margin-bottom:10px;'>");
                sb.append("<b>1. Vehicle Information</b><br/>");
                sb.append("Vehicle No: " + safe(invoice.getVehicleNo()) + "<br/>");
                sb.append("Driver Contact: " + safe(invoice.getDriverNo()) + "<br/>");
                sb.append("</div>");

                // Section 2 - Address Details
                sb.append("<div style='font-weight:bold; margin-bottom:4px;'>2. Address Details</div>");
                sb.append("<table style='width:100%; border-collapse:collapse; font-size:12px;'>");
                sb.append("<tr>");
                sb.append("<td style='width:50%; vertical-align:top;'>");
                sb.append("<b>From</b><br/>");
                sb.append(safe(invoice.getSupplierFirmName()) + "<br/>");
                sb.append("GSTIN: " + safe(invoice.getSupplierGstin()) + "<br/>");
                sb.append(safe(invoice.getSupplierAddress()) + "<br/><br/>");
                sb.append("</td>");
                sb.append("<td style='width:50%; vertical-align:top;'>");
                sb.append("<b>To</b><br/>");
                sb.append(safe(invoice.getCustomerName()) + "<br/>");
                sb.append("GSTIN: " + safe(invoice.getCustomerGstin()) + "<br/>");
                sb.append(safe(invoice.getCustomerAddress()) + "<br/>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("</table>");
                sb.append("<hr style='border:1px solid #444; margin:8px 0;'/>");

                // Section 3 - Goods Details
                sb.append("<div style='font-weight:bold; margin-bottom:4px;'>3. Goods Details</div>");
                sb.append("<table style='width:100%; border-collapse:collapse; font-size:12px; border:1px solid #444;'>");
                sb.append("<thead><tr style='background:#f3f3f3;'>");
                sb.append("<th style='border:1px solid #444; padding:4px;'>HSN Code</th>");
                sb.append("<th style='border:1px solid #444; padding:4px;'>Product Name &amp; Desc</th>");
                sb.append("<th style='border:1px solid #444; padding:4px;'>Quantity</th>");
                sb.append("<th style='border:1px solid #444; padding:4px;'>Taxable Amt</th>");
                sb.append("</tr></thead><tbody>");

                for (var item : invoice.getItems()) {
                        sb.append("<tr>");
                        sb.append("<td style='border:1px solid #444; padding:4px;'>" + safe(item.getHsnSac())
                                        + "</td>");
                        sb.append("<td style='border:1px solid #444; padding:4px;'>" + safe(item.getDescription())
                                        + "</td>");
                        sb.append("<td style='border:1px solid #444; padding:4px; text-align:right;'>"
                                        + item.getQuantityKg() + " KG</td>");
                        sb.append("<td style='border:1px solid #444; padding:4px; text-align:right;'>"
                                        + moneyFmt.format(item.getAmount()) + "</td>");
                        sb.append("</tr>");
                }

                sb.append("</tbody></table>");
                sb.append("</div>");
                sb.append("</body></html>");

                return sb.toString();

        }

        private String buildPurchaseInvoiceHtml(PurchaseInvoice invoice) {
                StringBuilder sb = new StringBuilder();
                sb.append(htmlHeader("Purchase Invoice"));

                // === HEADER SECTION ===
                sb.append("<table style='width:100%; border-collapse:collapse; font-family:Arial; font-size:12px;'>");
                sb.append("<tr>");
                sb.append("<td style='width:50%; vertical-align:top;'>");
                sb.append("<div style='font-weight:bold; font-size:14px;'>")
                                .append(safe(invoice.getSupplierFirmOwnerName()))
                                .append("</div>");
                sb.append("<div>").append(safe(invoice.getSupplierAddress())).append("</div>");
                sb.append("<div>Contact: ").append(safe(invoice.getSupplierMobile())).append("</div>");
                sb.append("</td>");

                sb.append("<td style='width:50%; vertical-align:top; text-align:right;'>");
                sb.append("<div><b>Invoice No:</b> ").append(safe(invoice.getInvoiceNumber())).append("</div>");
                sb.append("<div><b>Date:</b> ").append(formatDate(invoice.getInvoiceDate())).append("</div>");
                sb.append("<div><b>Vehicle No:</b> ").append(safe(invoice.getVehicleNumber())).append("</div>");
                sb.append("<div><b>Driver:</b> ").append(safe(invoice.getDriverName())).append(" (")
                                .append(safe(invoice.getDriverContactNumber())).append(")</div>");
                sb.append("</td>");
                sb.append("</tr>");
                sb.append("</table>");

                // === PARTY DETAILS ===
                sb.append("<div style='margin-top:10px; text-align:center; font-weight:bold; font-size:14px;'>INVOICE</div>");
                sb.append("<div style='text-align:center; font-size:12px; margin-bottom:8px;'>Party</div>");
                sb.append("<div style='text-align:center; font-size:12px;'>")
                                .append("<b>Morya Agro Foods</b><br/>Pune Solapur Road Near Kanchan Veg A/P Yawat, Tal: Daund, Dist: Pune - 412214<br/>")
                                .append("Contact: 9921557799 / 9921771799<br/>Email: moryaagrofoods79@gmail.com</div>");

                // === ITEM TABLE ===
                sb.append("<table style='width:100%; border-collapse:collapse; margin-top:10px; font-size:12px;'>");
                sb.append("<thead>");
                sb.append("<tr style='background:#f3f3f3;'>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>S</th>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>Description of Goods</th>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>Quantity</th>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>Rate</th>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>Per</th>");
                sb.append("<th style='border:1px solid #444; padding:5px;'>Amount</th>");
                sb.append("</tr>");
                sb.append("</thead><tbody>");

                // items loop — replacement that nests a small two-column table inside the
                // Description cell
                int sr = 1;
                double totalQty = 0.0;
                double totalAmount = 0.0;

                if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                        for (PurchaseItem item : invoice.getItems()) {
                                double amount = (item.getWeightKg() != null && item.getPricePerKg() != null)
                                                ? item.getWeightKg() * item.getPricePerKg()
                                                : 0.0;

                                sb.append("<tr>");

                                // S
                                sb.append("<td style='border:1px solid #444; padding:5px; text-align:center; vertical-align:top;'>")
                                                .append(sr++)
                                                .append("</td>");

                                // Description of Goods — nested table (two columns) to align "Less:" left and
                                // label right
                                sb.append("<td style='border:1px solid #444; padding:5px; vertical-align:top;'>");
                                // main item name
                                sb.append("<div style='font-weight:bold; margin-bottom:6px;'>")
                                                .append(safe(item.getItemName()))
                                                .append("</div>");
                                // nested table with two columns: left label (Less/Add) and right description
                                sb.append("<table style='width:100%; border-collapse:collapse; font-size:11px;'>");
                                // Weight Shortage row
                                sb.append("<tr>");
                                sb.append("<td style='padding:0; width:50%;'>")
                                                .append("<div style='padding:2px 4px;'>Less:</div>")
                                                .append("</td>");
                                sb.append("<td style='padding:0; width:50%; text-align:right;'>")
                                                .append("<div style='padding:2px 4px;'>Weight Shortage</div>")
                                                .append("</td>");
                                sb.append("</tr>");
                                // Chothari row
                                sb.append("<tr>");
                                sb.append("<td style='padding:0;'>")
                                                .append("<div style='padding:2px 4px;'>Less:</div>")
                                                .append("</td>");
                                sb.append("<td style='padding:0; text-align:right;'>")
                                                .append("<div style='padding:2px 4px;'>Chothari</div>")
                                                .append("</td>");
                                sb.append("</tr>");
                                // Weight Charges row
                                sb.append("<tr>");
                                sb.append("<td style='padding:0;'>")
                                                .append("<div style='padding:2px 4px;'>Less:</div>")
                                                .append("</td>");
                                sb.append("<td style='padding:0; text-align:right;'>")
                                                .append("<div style='padding:2px 4px;'>Weight Charges</div>")
                                                .append("</td>");
                                sb.append("</tr>");
                                // Round Up (Add) row
                                sb.append("<tr>");
                                sb.append("<td style='padding:0;'>")
                                                .append("<div style='padding:2px 4px;'>Add:</div>")
                                                .append("</td>");
                                sb.append("<td style='padding:0; text-align:right;'>")
                                                .append("<div style='padding:2px 4px;'>Round Up</div>")
                                                .append("</td>");
                                sb.append("</tr>");
                                sb.append("</table>");

                                sb.append("</td>"); // end description cell

                                // Quantity
                                sb.append("<td style='border:1px solid #444; padding:5px; text-align:right; vertical-align:top;'>")
                                                .append(formatNumber(item.getWeightKg())).append(" Kg</td>");

                                // Rate
                                sb.append("<td style='border:1px solid #444; padding:5px; text-align:right; vertical-align:top;'>")
                                                .append(formatMoney(item.getPricePerKg())).append("</td>");

                                // Per
                                sb.append("<td style='border:1px solid #444; padding:5px; text-align:center; vertical-align:top;'>Kg</td>");

                                // Amount column: main amount + stacked deduction/addition numbers (right
                                // aligned)
                                sb.append("<td style='border:1px solid #444; padding:5px; text-align:right; vertical-align:top;'>");
                                // main item amount
                                sb.append("<div>").append(formatMoney(amount)).append("</div>");
                                // weight shortage (global invoice field shown as a stacked deduction)
                                sb.append("<div style='margin-top:6px;'>(")
                                                .append(formatMoney(invoice.getWeightShortageAmount()))
                                                .append(")</div>");
                                // chotari
                                sb.append("<div>(").append(formatMoney(invoice.getChotariAmount())).append(")</div>");
                                // weight charges deducted (round up)
                                sb.append("<div>(").append(formatMoney(invoice.getWeightChargesDeducted()))
                                                .append(")</div>");
                                // NOTE: we keep logic unchanged — these values come from invoice-level fields
                                sb.append("</td>");

                                sb.append("</tr>");

                                totalQty += item.getWeightKg() != null ? item.getWeightKg() : 0.0;
                                totalAmount += amount;
                        }
                } else {
                        sb.append("<tr><td colspan='6' style='border:1px solid #444; text-align:center; padding:6px;'>No items available</td></tr>");
                }

                // === TOTAL ROW ===
                sb.append("<tr style='background:#f6f6f6; font-weight:bold;'>");
                sb.append("<td style='border:1px solid #444; text-align:left;'></td>");
                sb.append("<td style='border:1px solid #444; text-align:left;' colspan='2'>Total</td>");
                sb.append("<td style='border:1px solid #444; text-align:right;'>")
                                .append(formatNumber(totalQty)).append(" Kg</td>");
                sb.append("<td style='border:1px solid #444;'></td>");
                sb.append("<td style='border:1px solid #444; text-align:right;'>\u20B9 ")
                                .append(formatMoney(invoice.getFinalNetTotal())).append("</td>");
                sb.append("</tr>");

                sb.append("</tbody></table>");
                sb.append("<div style='margin-top:10px; text-align:right; font-size:10px;'>E &amp; O E</div>");

                sb.append("<div style='margin-top:10px; font-size:11px;'>");
                sb.append("<b>Remark:</b> Through N/HAB2MB691, MAULI<br/>");
                sb.append("<b>Company's Sales Tax No:</b><br/>");
                sb.append("<b>Buyer's Sales Tax No:</b><br/>");
                sb.append("<b>Company's CST No:</b><br/>");
                sb.append("</div>");

                // === SIGNATURE ===
                sb.append("<div style='margin-top:30px; text-align:right; font-weight:bold;'>for ")
                                .append(safe(invoice.getSupplierFirmOwnerName()))
                                .append("<br/><br/>Authorized Signatory</div>");

                sb.append("<div style='margin-top:6px; text-align:center; font-size:10px;'>This is a Computer Generated Invoice</div>");

                sb.append("</body></html>");
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

                                + "</style></head><body><h2 style='text-align:center; '>" + title + "</h2>";
        }

        // private String htmlFooter() {
        // return "<div style='margin-top:25px;'>"
        // + "<div style='float:left; width:50%'><p>Prepared by:
        // ____________________</p></div>"
        // + "<div style='float:right; width:50%; text-align:right;'><p>Authorised
        // Signatory</p></div>"
        // + "<div style='clear:both;'></div>"
        // + "</div></body></html>";
        // }

        private String nvl(String s) {
                return s == null ? "" : s;
        }

        private String nvlInt(Number n) {
                return n == null ? "" : String.valueOf(n);
        }

        private String formatMoney(Number n) {
                if (n == null)
                        return "0.00";
                return moneyFmt.format(n.doubleValue());
        }

        private String formatNumber(Number n) {
                if (n == null)
                        return "";
                return moneyFmt.format(n.doubleValue());
        }

        private String formatDate(java.time.LocalDate date) {
                return date == null ? "" : date.format(dateFmt);
        }

        private String safeId(Long id) {
                return id == null ? "0" : String.valueOf(id);
        }

        private String safe(Object val) {
                return val == null ? "" : val.toString();
        }

}