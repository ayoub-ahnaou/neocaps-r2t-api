package com.neocaps.api.service;

import com.neocaps.api.exception.AppValidationException;
import com.neocaps.api.exception.ResourceNotFoundException;
import com.neocaps.api.model.dto.LotCreateRequest;
import com.neocaps.api.model.dto.LotResponse;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.Lot;
import com.neocaps.api.repository.CapsuleRepository;
import com.neocaps.api.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final CapsuleRepository capsuleRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public LotResponse createLot(LotCreateRequest request) {
        if (lotRepository.existsBySupplierLotNumber(request.getSupplierLotNumber()))
            throw new AppValidationException("Lot with supplier number '" + request.getSupplierLotNumber() + "' already exists");

        if (lotRepository.existsByProductName(request.getProductName()))
            throw new AppValidationException("Lot with product name '" + request.getProductName() + "' already exists");

        Lot lot = Lot.builder()
                .productName(request.getProductName())
                .supplierLotNumber(request.getSupplierLotNumber())
                .totalActivityMci(request.getTotalActivityMci())
                .radioactiveConcentration(request.getRadioactiveConcentration())
                .reservoirVolumeMicroliter(request.getReservoirVolumeMicroliter())
                .manufacturingDate(request.getManufacturingDate())
                .calibrationDate(request.getCalibrationDate())
                .build();

        Lot savedLot = lotRepository.save(lot);

        auditLogService.log("CREATE_LOT", "Created Lot " + savedLot.getSupplierLotNumber() + " with " +
                savedLot.getTotalActivityMci() + " mCi and concentration " + savedLot.getRadioactiveConcentration() + " mCi/uL");

        return mapToResponse(savedLot);
    }

    public List<LotResponse> getAllLots() {
        return lotRepository.findAll().stream()
                .sorted(Comparator.comparing(Lot::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LotResponse getLotById(UUID id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found with ID: " + id));
        return mapToResponse(lot);
    }

    public LotResponse getLotBySupplierNumber(String supplierLotNumber) {
        Lot lot = lotRepository.findBySupplierLotNumber(supplierLotNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found with supplier number: " + supplierLotNumber));
        return mapToResponse(lot);
    }

    public LotResponse mapToResponse(Lot lot) {
        List<Capsule> capsules = capsuleRepository.findByLot(lot);
        double totalUsedVolume = capsules.stream()
                .mapToDouble(Capsule::getVolumeMicroliter)
                .sum();

        double remainingVolume = Math.max(0.0, lot.getReservoirVolumeMicroliter() - totalUsedVolume);
        long capsuleCount = capsules.size();

        return LotResponse.builder()
                .id(lot.getId())
                .productName(lot.getProductName())
                .supplierLotNumber(lot.getSupplierLotNumber())
                .totalActivityMci(lot.getTotalActivityMci())
                .radioactiveConcentration(lot.getRadioactiveConcentration())
                .reservoirVolumeMicroliter(lot.getReservoirVolumeMicroliter())
                .manufacturingDate(lot.getManufacturingDate())
                .calibrationDate(lot.getCalibrationDate())
                .remainingVolumeMicroliter(remainingVolume)
                .capsuleCount(capsuleCount)
                .createdAt(lot.getCreatedAt())
                .updatedAt(lot.getUpdatedAt())
                .build();
    }
}
