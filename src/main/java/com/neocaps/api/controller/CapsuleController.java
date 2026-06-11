package com.neocaps.api.controller;

import com.neocaps.api.exception.ResourceNotFoundException;
import com.neocaps.api.model.dto.CapsuleCreateRequest;
import com.neocaps.api.model.dto.CapsuleResponse;
import com.neocaps.api.model.dto.TrayStatusResponse;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.repository.CapsuleRepository;
import com.neocaps.api.service.BarcodeService;
import com.neocaps.api.service.CapsuleLabelPrinterService;
import com.neocaps.api.service.CapsuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/capsules")
@RequiredArgsConstructor
@CrossOrigin
public class CapsuleController {

    private final CapsuleService capsuleService;
    private final BarcodeService barcodeService;
    private final CapsuleLabelPrinterService printerService;
    private final CapsuleRepository capsuleRepository;

    @PostMapping
    public ResponseEntity<CapsuleResponse> generateCapsule(@Valid @RequestBody CapsuleCreateRequest request) {
        CapsuleResponse response = capsuleService.generateCapsule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CapsuleResponse>> getAllCapsules() {
        return ResponseEntity.ok(capsuleService.getAllCapsules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapsuleResponse> getCapsuleById(@PathVariable UUID id) {
        return ResponseEntity.ok(capsuleService.getCapsuleById(id));
    }

    @GetMapping("/tray")
    public ResponseEntity<List<TrayStatusResponse>> getTrayStatus() {
        return ResponseEntity.ok(capsuleService.getTrayStatus());
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<CapsuleResponse> getCapsuleByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(capsuleService.getCapsuleByBarcode(barcode));
    }

    @GetMapping("/{id}/barcode/image")
    public ResponseEntity<byte[]> getBarcodeImage(@PathVariable UUID id) {
        CapsuleResponse capsule = capsuleService.getCapsuleById(id);
        byte[] imageBytes = barcodeService.generateCode128Image(capsule.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);
        headers.setCacheControl("max-age=86400"); // cache for 1 day

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/print")
    public ResponseEntity<String> printCapsule(@PathVariable String id) { // 1. Retiré le @RequestBody inutile
        try {
            // 2. Récupération de l'objet contenant le code-barres compressé Base62
            Capsule capsule = capsuleRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new ResourceNotFoundException("Capsule not found with ID: " + id));

            // 3. Envoi au service avec le nom de l'imprimante Windows en dur
            printerService.printCapsuleLabel("Gainscha GS-2408DC", capsule);

            return ResponseEntity.ok("Print job successfully queued to Gainscha printer.");
        } catch (IOException | javax.print.PrintException e) {
            // 4. Ajout de PrintException ici pour que Java compile sans erreur
            return ResponseEntity.internalServerError().body("Printer communication error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
