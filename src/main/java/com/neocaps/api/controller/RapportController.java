package com.neocaps.api.controller;

import com.neocaps.api.exception.ResourceNotFoundException;
import com.neocaps.api.model.dto.RapportResponse;
import com.neocaps.api.model.entity.Rapport;
import com.neocaps.api.repository.RapportRepository;
import com.neocaps.api.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;
    private final RapportRepository rapportRepository;

    @PostMapping("/generate/{lotId}")
    public ResponseEntity<RapportResponse> generateRapport(@PathVariable String lotId) {
        RapportResponse rapport = rapportService.createRapportForLot(lotId);
        return ResponseEntity.ok(rapport);
    }
    
    @GetMapping
    public ResponseEntity<List<RapportResponse>> getAllRapports() {
        return ResponseEntity.ok(rapportService.getAllRapports());
    }

    @GetMapping("/download/{rapportId}")
    public ResponseEntity<?> downloadRapport(@PathVariable String rapportId) {
        try {
            Rapport rapport = rapportRepository.findById(UUID.fromString(rapportId))
                    .orElseThrow(() -> new ResourceNotFoundException("Rapport not found"));

            File file = new File(rapport.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = java.nio.file.Files.readAllBytes(file.toPath());

            String customFilename = "rapport_lot_" + rapport.getLot().getSupplierLotNumber() + "_" + Instant.now().toEpochMilli() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    // Pass your specific filename variable here
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + customFilename + "\"")
                    // Expose the header to the browser frontend framework explicitly
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error downloading: " + e.getMessage());
        }
    }
}
