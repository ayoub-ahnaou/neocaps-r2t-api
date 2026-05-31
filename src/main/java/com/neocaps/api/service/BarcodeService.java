package com.neocaps.api.service;

import org.springframework.stereotype.Service;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Service
public class BarcodeService {

    // Code 128 character patterns (index 0 to 102 correspond to values 0 to 102)
    private static final String[] CODE128_PATTERNS = {
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213", // 0-9
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132", // 10-19
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211", // 20-29
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211312", // 30-39
        "231112", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331", // 40-49
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", // 50-59
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214", // 60-69
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111", // 70-79
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141", // 80-89
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141", // 90-99
        "114131", "311141", "411131" // 100-102
    };

    private static final String START_B = "211214"; // Value 104
    private static final String STOP = "2331112";   // Value 106

    /**
     * Generates standard barcode text based on Lot supplier number and capsule display number.
     * E.g. "R2T-LOT001-CAPS05"
     */
    public String generateBarcodeText(String supplierLotNumber, String capsuleSequenceStr) {
        return "R2T-" + supplierLotNumber + "-" + capsuleSequenceStr;
    }

    /**
     * Dynamically generates a Code128 (Type B) barcode image for printing.
     *
     * @param text The barcode text to encode.
     * @return PNG image bytes.
     */
    public byte[] generateCode128Image(String text) {
        try {
            // Validate input: Code 128 Type B supports standard printable ASCII
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c > 127) {
                    throw new IllegalArgumentException("Unsupported character in barcode text: " + c);
                }
            }

            // Calculate check digit
            int sum = 104; // Start B value
            for (int i = 0; i < text.length(); i++) {
                int charVal = text.charAt(i) - 32;
                sum += charVal * (i + 1);
            }
            int checkDigit = sum % 103;

            // Build encoded widths sequence
            StringBuilder widths = new StringBuilder();
            widths.append(START_B);

            for (int i = 0; i < text.length(); i++) {
                int charVal = text.charAt(i) - 32;
                widths.append(CODE128_PATTERNS[charVal]);
            }

            // Append check digit pattern
            widths.append(CODE128_PATTERNS[checkDigit]);
            // Append stop pattern
            widths.append(STOP);

            // Render barcode image
            // We define module width (pixel width of a single unit) as 2
            int moduleWidth = 2;
            int quietZoneWidth = 20; // quiet zones on both sides
            int barcodeHeight = 80;
            int textSpaceHeight = 20;
            
            // Total modules count is sum of width digits
            int totalModules = 0;
            for (int i = 0; i < widths.length(); i++) {
                totalModules += Character.getNumericValue(widths.charAt(i));
            }

            int imageWidth = totalModules * moduleWidth + (quietZoneWidth * 2);
            int imageHeight = barcodeHeight + textSpaceHeight + 10;

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, imageWidth, imageHeight);

            // Draw bars
            g2.setColor(Color.BLACK);
            int currentX = quietZoneWidth;
            boolean isBar = true; // start with a bar, alternate bar/space

            for (int i = 0; i < widths.length(); i++) {
                int widthVal = Character.getNumericValue(widths.charAt(i));
                int pixelWidth = widthVal * moduleWidth;

                if (isBar) {
                    g2.fillRect(currentX, 10, pixelWidth, barcodeHeight);
                }

                currentX += pixelWidth;
                isBar = !isBar; // toggle between bar and space
            }

            // Draw text below barcode
            g2.setColor(Color.BLACK);
            g2.drawString(text, imageWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2, barcodeHeight + 25);

            g2.dispose();

            // Convert to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate barcode image", e);
        }
    }
}
