package com.neocaps.api.service;

import com.neocaps.api.model.entity.Capsule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
@Service
public class CapsuleLabelPrinterService {
    /**
     * Génère le ZPL et l'envoie à l'imprimante Gainscha connectée en USB sous Windows.
     * @param printerName Le nom exact de votre imprimante sous Windows (ex: "Gainscha GS-2408DC")
     */
    public void printCapsuleLabel(String printerName, Capsule capsule) throws IOException, PrintException {
        String zplPayload = buildZplPayload(capsule);
        sendToWindowsUsbPrinter(printerName, zplPayload);
    }

    /**
     * Utilise l'API d'impression Java pour injecter le ZPL brut dans le spouleur Windows.
     */
    private void sendToWindowsUsbPrinter(String printerName, String zplData) throws PrintException {
        // 1. Définir le format de document comme "Données brutes" (Autosense)
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

        // 2. Rechercher l'imprimante par son nom Windows exact
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
        PrintService targetService = null;

        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                targetService = service;
                break;
            }
        }

        // Si l'imprimante spécifiée n'est pas trouvée, on prend celle par défaut
        if (targetService == null) {
            targetService = PrintServiceLookup.lookupDefaultPrintService();
            if (targetService == null) {
                throw new PrintException("Aucune imprimante disponible sur ce système.");
            }
        }

        // 3. Préparer le flux de données avec le ZPL
        ByteArrayInputStream stream = new ByteArrayInputStream(zplData.getBytes(StandardCharsets.US_ASCII));
        Doc doc = new SimpleDoc(stream, flavor, null);

        // 4. Crier le job d'impression et l'envoyer
        DocPrintJob job = targetService.createPrintJob();
        job.print(doc, null);
    }

    /**
     * Le layout ultra-compact (2.90cm x 2.00cm)
     */
    private String buildZplPayload(Capsule capsule) {
        return "^XA\n" +
                "^PW231\n" +                       // Largeur: 231 dots (2.90cm)
                "^LL160\n" +                       // Hauteur: 160 dots (2.00cm)

                // 1. Texte ID
                "^FO20,15^A0N,15,15^FDID: " + capsule.getId() + "^FS\n" +

                // 2. Code-barres compact (Module à 1 dot)
                "^BY1,2.0,70^FT20,110\n" +
                "^BCN,70,Y,N,N^FD" + capsule.getBarcode() + "^FS\n" +

                "^XZ";
    }
}
