package com.r2p.neocaps.service;

import com.r2p.neocaps.aop.Auditable;
import com.r2p.neocaps.entity.Capsule;
import com.r2p.neocaps.repository.CapsuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.r2p.neocaps.entity.enums.CapsuleStatus;
import com.r2p.neocaps.entity.enums.RobotActionType;
import com.r2p.neocaps.entity.RobotAction;
import com.r2p.neocaps.repository.RobotActionRepository;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionService {

    private final CapsuleRepository capsuleRepository;
    private final PLCService plcService;
    private final RobotActionRepository robotActionRepository;

    @Transactional
    @Auditable(action = "PROCESS_CAPSULE", description = "Started production process for a capsule")
    public void processCapsule(UUID capsuleId, String scannedBarcode) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new IllegalArgumentException("Capsule not found"));

        if (!capsule.getBarcode().equals(scannedBarcode)) {
            throw new IllegalArgumentException("Barcode mismatch! Expected: " + capsule.getBarcode() + ", Scanned: " + scannedBarcode);
        }

        log.info("Barcode validated for {}. Starting PLC process...", scannedBarcode);
        
        capsule.setStatus(CapsuleStatus.FILLED);
        capsuleRepository.save(capsule);

        // Record Action
        RobotAction action = RobotAction.builder()
                .capsule(capsule)
                .actionType(RobotActionType.FILL)
                .fromPosition("TRAY-" + capsule.getTrayPosition())
                .toPosition("RACK-" + capsule.getRackNumber() + "-" + capsule.getRackPosition())
                .timestamp(LocalDateTime.now())
                .success(true)
                .message("Started PLC fill process")
                .build();
        robotActionRepository.save(action);

        // Call PLCService
        plcService.writeProductionData(capsule.getTrayPosition(), capsule.getVolumeMicroliter());
        plcService.triggerStartPulse();
    }
}
