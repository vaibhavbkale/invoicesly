package com.vaibhavbkale.invoicesly.service;

import com.vaibhavbkale.invoicesly.model.Invoice;
import com.vaibhavbkale.invoicesly.model.InvoiceItem;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfServiceImpl implements PdfService {

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            float y = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(margin, y);
            cs.showText("Invoice: " + invoice.getInvoiceNumber());
            cs.endText();

            y -= 25;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(margin, y);
            cs.showText("Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ISO_DATE));
            cs.endText();

            y -= 20;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(margin, y);
            cs.showText("Customer: " + (invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A"));
            cs.endText();

            y -= 30;
            // table header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(margin, y);
            cs.showText(String.format("%-60s %8s %12s %12s", "Description", "Qty", "Unit", "Total"));
            cs.endText();

            y -= 15;
            cs.setFont(PDType1Font.HELVETICA, 10);
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            for (InvoiceItem item : invoice.getItems()) {
                if (y < 100) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - 50;
                }
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                String desc = item.getDescription() == null ? "" : item.getDescription();
                String line = String.format("%-60.60s %8d %12s %12s",
                        desc,
                        item.getQuantity(),
                        nf.format(item.getUnitPrice()),
                        nf.format(item.getLineTotal()));
                cs.showText(line);
                cs.endText();
                y -= 12;
            }

            y -= 20;
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.showText("Subtotal: " + nf.format(invoice.getSubtotal()));
            cs.endText();

            y -= 14;
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.showText("Tax: " + nf.format(invoice.getTaxTotal()));
            cs.endText();

            y -= 14;
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.showText("Total: " + nf.format(invoice.getTotal()));
            cs.endText();

            cs.close();

            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate PDF", ex);
        }
    }
}
