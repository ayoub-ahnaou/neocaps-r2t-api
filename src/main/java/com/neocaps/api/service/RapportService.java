package com.neocaps.api.service;

import com.neocaps.api.exception.ResourceNotFoundException;
import com.neocaps.api.model.dto.RapportResponse;
import com.neocaps.api.model.entity.Lot;
import com.neocaps.api.model.entity.Rapport;
import com.neocaps.api.repository.LotRepository;
import com.neocaps.api.repository.RapportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportService {

    private final RapportRepository rapportRepository;
    private final LotRepository lotRepository;
    private final RapportPdfGeneratorService pdfService;

    @Transactional
    public RapportResponse createRapportForLot(String lotId) {
        // Find lot
        Lot lot = lotRepository.findById(UUID.fromString(lotId))
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found with id: " + lotId));

        // Generate PDF
        String pdfPath = pdfService.generateRapportPdf(lot);

        // Create rapport record
        Rapport rapport = new Rapport();
        rapport.setCreatedAt(LocalDateTime.now());
        rapport.setFilePath(pdfPath);
        rapport.setLot(lot);

        return mapToRapportResponse(rapportRepository.save(rapport));
    }

    public List<RapportResponse> getAllRapports() {
        List<Rapport> rapports = this.rapportRepository.findAll();
        return rapports.stream().map(this::mapToRapportResponse).toList();
    }

    private RapportResponse mapToRapportResponse(Rapport rapport) {
        return RapportResponse.builder()
                .id(rapport.getId())
                .title(rapport.getTitle())
                .createdAt(rapport.getCreatedAt())
                .generatedBy(rapport.getGeneratedBy())
                .filePath(rapport.getFilePath())
                .lot(rapport.getLot())
                .build();
    }
}
