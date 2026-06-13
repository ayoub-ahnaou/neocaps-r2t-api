package com.neocaps.api.service;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.Lot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.awt.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportPdfGeneratorService {

    // Modern Pharmaceutical Palette
    private static final Color PRIMARY_COLOR = new Color(15, 23, 42);     // Deep Slate / Charcoal
    private static final Color ACCENT_MED = new Color(14, 116, 144);     // Teal / Nuclear Medicine Cyan
    private static final Color TEXT_DARK = new Color(51, 65, 85);        // Body Text
    private static final Color BG_LIGHT = new Color(248, 250, 252);      // Alternating row background
    private static final Color BORDER_COLOR = new Color(226, 232, 240);  // Subtle borders

    private static final String REPORTS_DIRECTORY = "generated_reports/";

    public String generateRapportPdf(Lot lot) {
        new java.io.File(REPORTS_DIRECTORY).mkdirs();

        String fileName = "rapport_lot_" + lot.getSupplierLotNumber() + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = REPORTS_DIRECTORY + fileName;

        Document document = new Document(PageSize.A4, 36, 36, 45, 54);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            writer.setPageEvent(new PdfFooterEventHandler());

            document.open();

            // ==========================================
            // 1. HEADER & LOGO IMAGE INTEGRATION
            // ==========================================
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{7.0f, 3.0f}); // Adjusted layout widths slightly for the image box

            // Title block
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
            Paragraph mainTitle = new Paragraph("BATCH PRODUCTION REPORT", titleFont);
            mainTitle.setSpacingAfter(2f);
            titleCell.addElement(mainTitle);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            Paragraph subTitle = new Paragraph("Radiopharmaceutical Manufacturing Record", subFont);
            titleCell.addElement(subTitle);
            headerTable.addCell(titleCell);

            // LOGO IMAGE LOADING BLOCK
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                // Safely load the image from the classpath using resources path
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/r2t-industrie-logo.jpg");

                if (is != null) {
                    byte[] imageBytes = is.readAllBytes();
                    Image logoImg = Image.getInstance(imageBytes);

                    // Scale the image seamlessly to fit a nice header boundarybox
                    // (e.g. max width 100px, max height 45px depending on your logo's aspect ratio)
                    logoImg.scaleToFit(100f, 45f);
                    logoImg.setAlignment(Element.ALIGN_RIGHT);
                    logoCell.addElement(logoImg);
                } else {
                    // Fallback to text placeholder if file is missing/renamed by accident
                    Font fallbackFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, ACCENT_MED);
                    Paragraph fallbackText = new Paragraph("[ COMPANY LOGO ]", fallbackFont);
                    fallbackText.setAlignment(Element.ALIGN_RIGHT);
                    logoCell.addElement(fallbackText);
                }
            } catch (Exception imgEx) {
                // Prevent complete PDF generation failure if image decoding errors occur
                System.err.println("Could not load header logo asset: " + imgEx.getMessage());
            }

            headerTable.addCell(logoCell);
            document.add(headerTable);
            document.add(createDivider(1.5f, ACCENT_MED, 15f));

            // ==========================================
            // 2. LOT SPECIFICATIONS (TWO-COLUMN INFOCARD)
            // ==========================================
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, ACCENT_MED);
            Paragraph specTitle = new Paragraph("LOT SPECIFICATIONS", sectionFont);
            specTitle.setSpacingAfter(8f);
            document.add(specTitle);

            PdfPTable specsTable = new PdfPTable(2);
            specsTable.setWidthPercentage(100);
            specsTable.setWidths(new float[]{5f, 5f});

            PdfPTable leftMeta = new PdfPTable(2);
            leftMeta.setWidths(new float[]{4.5f, 5.5f});
            addMetaField(leftMeta, "Product Name:", lot.getProductName());
            addMetaField(leftMeta, "Supplier Lot #:", lot.getSupplierLotNumber());
            addMetaField(leftMeta, "Mfg Date:", formatDate(lot.getManufacturingDate()));
            addMetaField(leftMeta, "Created At:", formatDateTime(lot.getCreatedAt()));

            PdfPCell leftCell = new PdfPCell(leftMeta);
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPaddingRight(10f);
            specsTable.addCell(leftCell);

            PdfPTable rightMeta = new PdfPTable(2);
            rightMeta.setWidths(new float[]{5.5f, 4.5f});
            addMetaField(rightMeta, "Total Activity:", lot.getTotalActivityMci() + " mCi");
            addMetaField(rightMeta, "Radioactive Conc:", lot.getRadioactiveConcentration() + " mCi/µL");
            addMetaField(rightMeta, "Reservoir Volume:", lot.getReservoirVolumeMicroliter() + " µL");
            addMetaField(rightMeta, "Calibration Date:", formatDate(lot.getCalibrationDate()));

            PdfPCell rightCell = new PdfPCell(rightMeta);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPaddingLeft(10f);
            specsTable.addCell(rightCell);

            document.add(specsTable);
            document.add(createDivider(0.5f, BORDER_COLOR, 20f));

            // ==========================================
            // 3. CAPSULES DISPENSING GRID
            // ==========================================
            Paragraph gridTitle = new Paragraph("DISPENSED CAPSULES LOG", sectionFont);
            gridTitle.setSpacingAfter(10f);
            document.add(gridTitle);

            List<Capsule> capsules = lot.getCapsules();
            if (capsules == null || capsules.isEmpty()) {
                Font emptyFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10, TEXT_DARK);
                document.add(new Paragraph("No individual capsules are logged against this manufacturing run.", emptyFont));
            } else {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3.2f, 1.8f, 2.0f, 2.0f, 2.0f});

                addTableHeader(table, "Capsule ID");
                addTableHeader(table, "Tray Pos.");
                addTableHeader(table, "Dose (mCi)");
                addTableHeader(table, "Volume (µL)");
                addTableHeader(table, "Status");

                boolean alternatingRow = false;
                for (Capsule capsule : capsules) {
                    Color rowBg = alternatingRow ? BG_LIGHT : Color.WHITE;

                    addTableCell(table, capsule.getId().toString(), rowBg, Element.ALIGN_LEFT);
                    addTableCell(table, capsule.getTrayPosition() != null ? String.valueOf(capsule.getTrayPosition()) : "-", rowBg, Element.ALIGN_CENTER);
                    addTableCell(table, capsule.getDoseMci() != null ? String.format("%.3f", capsule.getDoseMci()) : "0.0", rowBg, Element.ALIGN_RIGHT);
                    addTableCell(table, capsule.getVolumeMicroliter() != null ? String.format("%.2f", capsule.getVolumeMicroliter()) : "0.0", rowBg, Element.ALIGN_RIGHT);

                    addStatusBadgeCell(table, capsule.getStatus() != null ? capsule.getStatus().toString() : "UNKNOWN", rowBg);

                    alternatingRow = !alternatingRow;
                }
                document.add(table);

                Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
                Paragraph summaryPara = new Paragraph("Total Batched Units: " + capsules.size() + " Capsules", summaryFont);
                summaryPara.setAlignment(Element.ALIGN_RIGHT);
                summaryPara.setSpacingBefore(12f);
                document.add(summaryPara);
            }

            document.close();
            return filePath;

        } catch (Exception e) {
            throw new RuntimeException("Error executing PDF creation context: " + e.getMessage(), e);
        }
    }

    // Creates an elegant status badge with custom color styling logic
    private void addStatusBadgeCell(PdfPTable table, String status, Color rowBg) {
        Color badgeText = new Color(30, 41, 59);
        Color badgeBg;

        // Green theme for positive production cycles, amber for testing/pending states
        if ("FILLED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status) || "VALIDATED".equalsIgnoreCase(status)) {
            badgeText = new Color(21, 128, 61);
            badgeBg = new Color(220, 252, 231);
        } else if ("PENDING".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status)) {
            badgeText = new Color(180, 83, 9);
            badgeBg = new Color(254, 243, 199);
        } else {
            badgeBg = new Color(241, 245, 249);
        }

        Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, badgeText);
        PdfPCell cell = new PdfPCell(new Paragraph(status, badgeFont));
        cell.setBackgroundColor(rowBg);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);

        // This simulates a nested padded card block visually inside the clean cell box
        cell.setCellEvent((PdfPCell cellEvent, Rectangle rect, PdfContentByte[] canvases) -> {
            PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
            cb.saveState();
            cb.setColorFill(badgeBg);
            cb.roundRectangle(rect.getLeft() + 6, rect.getBottom() + 3, rect.getWidth() - 12, rect.getHeight() - 6, 4f);
            cb.fill();
            cb.restoreState();
        });

        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String headerText) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Paragraph(headerText, headerFont));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Color bgColor, int alignment) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
        PdfPCell cell = new PdfPCell(new Paragraph(text, cellFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(7f);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addMetaField(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PRIMARY_COLOR);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);

        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4f);

        PdfPCell valCell = new PdfPCell(new Paragraph(value != null ? value : "N/A", valFont));
        valCell.setBorder(Rectangle.NO_BORDER);
        valCell.setPadding(4f);

        table.addCell(labelCell);
        table.addCell(valCell);
    }

    private Paragraph createDivider(float thickness, Color color, float spacingAfter) {
        Paragraph p = new Paragraph();
        com.lowagie.text.pdf.draw.LineSeparator line = new com.lowagie.text.pdf.draw.LineSeparator();
        line.setLineColor(color);
        line.setLineWidth(thickness);
        p.add(line);
        p.setSpacingBefore(4f);
        p.setSpacingAfter(spacingAfter);
        return p;
    }

    // Draws a vector atomic/medical core geometric design context safely inline
    private void drawMedicalLogo(PdfTemplate template) {
        template.saveState();
        // Outer Rings
        template.setColorStroke(ACCENT_MED);
        template.setLineWidth(1.2f);
        template.ellipse(20, 10, 60, 30);
        template.stroke();

        template.ellipse(30, 5, 50, 35);
        template.stroke();

        // Central core element
        template.setColorFill(PRIMARY_COLOR);
        template.circle(40, 20, 5);
        template.fill();
        template.restoreState();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
    }

    // Custom Page numbering events implementation
    private static class PdfFooterEventHandler extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(148, 163, 184));

            String text = "Automated Production Log Verification | Page " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(text, font),
                    document.right(), document.bottom() - 20, 0);
        }
    }
}
