package com.neocaps.api.service;

import com.neocaps.api.enums.CapsuleStatus;
import com.neocaps.api.exception.AppValidationException;
import com.neocaps.api.exception.ResourceNotFoundException;
import com.neocaps.api.model.dto.CapsuleCreateRequest;
import com.neocaps.api.model.dto.CapsuleResponse;
import com.neocaps.api.model.dto.TrayStatusResponse;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.Lot;
import com.neocaps.api.repository.CapsuleRepository;
import com.neocaps.api.repository.LotRepository;
import com.neocaps.api.utils.KeyCompressor;
import com.neocaps.api.utils.NanoIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final LotRepository lotRepository;
    private final BarcodeService barcodeService;
    private final AuditLogService auditLogService;

    private static final List<Integer> EXCLUDED_TRAY_POSITIONS = Arrays.asList(45, 46, 55, 56);

    @Transactional
    public CapsuleResponse generateCapsule(CapsuleCreateRequest request) {
        // 1. Load lot
        Lot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found with ID: " + request.getLotId()));

        // 2. Check maximum capsules limit per lot (96)
        long currentCount = capsuleRepository.countByLot(lot);
        if (currentCount >= 96) {
            throw new AppValidationException("Lot " + lot.getSupplierLotNumber() + " has reached its maximum production limit of 96 capsules.");
        }

        // 3 & 4. Calculate radioactive decay & required injection volume
        // TODO: user later a formula to calculate the required volume

        // 5. Validate maximum allowed volume (reservoir capacity)
        double totalUsedVolume = capsuleRepository.findByLot(lot).stream()
                .mapToDouble(Capsule::getVolumeMicroliter)
                .sum();
        double remainingVolume = lot.getReservoirVolumeMicroliter() - totalUsedVolume;

//        if (requiredVolume > remainingVolume) {
//            throw new AppValidationException(String.format(
//                    "Insufficient radioactive liquid. Required: %.2f µL, Remaining: %.2f µL (Lot: %s)",
//                    requiredVolume, remainingVolume, lot.getSupplierLotNumber()
//            ));
//        }

        // 6. Find available tray position
        int trayPosition = findAvailableTrayPosition();

        // 7. Generate display identifier & barcode text
        // Format displayId: CAPS-XX (e.g. CAPS-05)
        String sequenceStr = String.format("%02d", currentCount + 1);
        String displayId = "CAPS-" + sequenceStr;
        
        // Ensure displayId is unique (in case of deletions or anomalies)
        int attempts = 1;
        while (capsuleRepository.existsByDisplayId(displayId)) {
            displayId = "CAPS-" + String.format("%02d", currentCount + 1 + attempts);
            sequenceStr = String.format("%02d", currentCount + 1 + attempts);
            attempts++;
        }

        // 9. Save Capsule
        Capsule capsule = Capsule.builder()
                .displayId(displayId)
                .trayPosition(trayPosition)
                //.rackNumber(rackAssignment.rackNumber)
                //.rackPosition(rackAssignment.rackPosition)
                .doseMci(request.getDoseMci())
                .volumeMicroliter(0.0)
                .clientReference(request.getClientReference())
                .status(CapsuleStatus.WAITING)
                .barcode(NanoIdGenerator.generateShortId())
                .manufacturingDate(request.getManufacturingDate())
                .calibrationDate(request.getCalibrationDate())
                .lot(lot)
                .build();

        Capsule savedCapsule = capsuleRepository.save(capsule);

        auditLogService.log("GENERATE_CAPSULE", String.format(
                "Generated Capsule %s for Lot %s. Tray Pos: %d, Vol: %.2f µL",
                savedCapsule.getId(), lot.getSupplierLotNumber(), savedCapsule.getTrayPosition(), savedCapsule.getVolumeMicroliter()
        ));

        return mapToResponse(savedCapsule);
    }

    public List<CapsuleResponse> getAllCapsules() {
        return capsuleRepository.findAll().stream()
                .sorted(Comparator.comparing(Capsule::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CapsuleResponse getCapsuleById(UUID id) {
        Capsule capsule = capsuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found with ID: " + id));
        return mapToResponse(capsule);
    }

    /**
     * Returns the physical status of all 96 positions in the production tray.
     */
    public List<TrayStatusResponse> getTrayStatus() {
        List<Capsule> activeCapsules = capsuleRepository.findAll().stream()
                .filter(c -> c.getStatus() != CapsuleStatus.STORED && c.getStatus() != CapsuleStatus.ERROR)
                .toList();

        List<TrayStatusResponse> tray = new ArrayList<>(96);
        for (int i = 1; i <= 96; i++) {
            boolean usable = !EXCLUDED_TRAY_POSITIONS.contains(i);
            final int pos = i;
            
            Optional<Capsule> matchingCapsule = activeCapsules.stream()
                    .filter(c -> c.getTrayPosition() == pos)
                    .findFirst();

            CapsuleResponse capsuleResponse = matchingCapsule.map(this::mapToResponse).orElse(null);

            tray.add(TrayStatusResponse.builder()
                    .position(pos)
                    .usable(usable)
                    .capsule(capsuleResponse)
                    .build());
        }
        return tray;
    }

    private int findAvailableTrayPosition() {
        for (int pos = 1; pos <= 96; pos++) {
            if (EXCLUDED_TRAY_POSITIONS.contains(pos)) {
                continue;
            }
            // A position is occupied if there is a capsule physically on it (status WAITING, FILLED, or PACKAGED)
            boolean occupied = capsuleRepository.existsByTrayPositionAndStatusNotIn(
                    pos,
                    List.of(CapsuleStatus.STORED, CapsuleStatus.ERROR)
            );
            if (!occupied) {
                return pos;
            }
        }
        throw new AppValidationException("Tray is full. All 92 usable tray positions are currently occupied.");
    }

    public CapsuleResponse getCapsuleByBarcode(String barcode) {
        Capsule capsule = capsuleRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Capsule not found with barcode: " + barcode));
        return mapToResponse(capsule);
    }

    public CapsuleResponse mapToResponse(Capsule capsule) {
        return CapsuleResponse.builder()
                .id(capsule.getId())
                .displayId(capsule.getDisplayId())
                .trayPosition(capsule.getTrayPosition())
                //.rackNumber(capsule.getRackNumber())
                //.rackPosition(capsule.getRackPosition())
                .doseMci(capsule.getDoseMci())
                .barcode(capsule.getBarcode())
                .volumeMicroliter(capsule.getVolumeMicroliter())
                .status(capsule.getStatus())
                .lotId(capsule.getLot().getId())
                .supplierLotNumber(capsule.getLot().getSupplierLotNumber())
                .clientReference(capsule.getClientReference())
                .manufacturingDate(capsule.getManufacturingDate())
                .calibrationDate(capsule.getCalibrationDate())
                .createdAt(capsule.getCreatedAt())
                .updatedAt(capsule.getUpdatedAt())
                .build();
    }
}
