package com.neocaps.api.service;

import com.neocaps.api.model.entity.Capsule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
@Service
public class CapsuleLabelPrinterService {
    // Default raw printing port for thermal printers
    private static final int PRINTER_PORT = 9100;

    /**
     * Generates ZPL from the Capsule and sends it to the printer.
     */
    public void printCapsuleLabel(String printerIp, Capsule capsule) throws IOException {
        String zplPayload = buildZplPayload(capsule);
        sendToPrinter(printerIp, zplPayload);
    }

    /**
     * Constructs the ZPL layout.
     * Adjust coordinates (^FO) based on your physical label size.
     */
    private String buildZplPayload(Capsule capsule) {
        return "^XA\n" +
                "^PW231\n" +                       // Precise width: 2.90cm (231 dots)
                "^LL160\n" +                       // Precise height: 2.00cm (160 dots)

                // 1. Database ID Text (Small font size 15x15, pushed right to the top left)
                "^FO20,15^A0N,15,15^FDID: " + capsule.getId() + "^FS\n" +

                // 2. Ultra-Compact Barcode
                // ^BY1 (1-dot narrow bar width) is strictly mandatory to fit within 231 dots.
                // Height is set to 70 dots, which takes up almost half of our vertical space.
                "^BY1,2.0,70^FT20,110\n" +
                "^BCN,70,Y,N,N^FD" + capsule.getBarcode() + "^FS\n" +

                "^XZ";
    }

    /**
     * Opens a raw socket connection and streams the bytes
     */
    private void sendToPrinter(String ip, String zpl) throws IOException {
        try (Socket socket = new Socket(ip, PRINTER_PORT);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            byte[] zplBytes = zpl.getBytes(StandardCharsets.US_ASCII);
            outputStream.write(zplBytes);
            outputStream.flush();
        }
    }
}
