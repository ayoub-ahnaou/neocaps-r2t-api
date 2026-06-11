package com.neocaps.api.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.Lot;
import com.neocaps.api.repository.LotRepository;
import com.neocaps.api.repository.RapportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportPdfGeneratorService {

    private LotRepository lotRepository;
    private RapportRepository rapportRepository;

    private static final String REPORTS_DIRECTORY = "generated_reports/";

    public String generateRapportPdf(Lot lot) {
        try {
            // Create folder if not exists
            new java.io.File(REPORTS_DIRECTORY).mkdirs();

            // Generate filename
            String fileName = "rapport_lot_" + lot.getSupplierLotNumber() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = REPORTS_DIRECTORY + fileName;

            // Create PDF document
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titlePara = new Paragraph("rapport_lot_" + lot.getSupplierLotNumber() + "_" + System.currentTimeMillis(), titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);

            document.add(new Paragraph(" ")); // Empty line

            // Add generation date
            document.add(new Paragraph("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            document.add(new Paragraph(" "));

            // Add Lot Information
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph("Lot Information", sectionFont));
            document.add(new Paragraph("Lot Number: " + lot.getSupplierLotNumber()));
            document.add(new Paragraph("Product Designation: " + lot.getProductName()));
            document.add(new Paragraph("Manufacturing Date: " + (lot.getManufacturingDate() != null ? lot.getManufacturingDate() : "N/A")));
            document.add(new Paragraph(" "));

            // Add Capsules Information
            document.add(new Paragraph("Capsules Filled with this Lot", sectionFont));
            document.add(new Paragraph(" "));

            List<Capsule> capsules = lot.getCapsules();
            if (capsules == null || capsules.isEmpty()) {
                document.add(new Paragraph("No capsules associated with this lot."));
            } else {
                // Create table with 4 columns
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);

                // Add headers
                addTableHeader(table, "Capsule Code");
                addTableHeader(table, "Type");
                addTableHeader(table, "Volume");
                addTableHeader(table, "Filling Date");

                // Add data rows
                for (Capsule capsule : capsules) {
                    table.addCell(capsule.getId().toString());
                    table.addCell(capsule.getVolumeMicroliter() != null ? capsule.getVolumeMicroliter().toString() + " mL" : "N/A");
                    table.addCell(capsule.getCreatedAt() != null ? capsule.getCreatedAt().toString() : "N/A");
                }

                document.add(table);
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Total capsules: " + capsules.size()));
            }

            document.close();
            return filePath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, String headerText) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Paragraph(headerText, headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
