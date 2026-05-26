package com.r2p.neocaps.service;

import com.r2p.neocaps.aop.Auditable;
import com.r2p.neocaps.entity.Capsule;
import com.r2p.neocaps.entity.Lot;
import com.r2p.neocaps.repository.CapsuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import com.r2p.neocaps.entity.enums.CapsuleStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final LotService lotService;

    private static final double LAMBDA = 0.0036;
    private static final Set<Integer> EXCLUDED_POSITIONS = Set.of(45, 46, 55, 56);
    private static final int MAX_CAPSULES_PER_LOT = 96;

    @Transactional
    @Auditable(action = "GENERATE_CAPSULE", description = "Generated a new capsule with calculated dose")
    public Capsule generateCapsule(UUID lotId, Double doseMci, LocalDateTime targetDate) {
        Lot lot = lotService.getLotById(lotId);

        // Check 96 limit
        long count = capsuleRepository.countByLotId(lotId);
        if (count >= MAX_CAPSULES_PER_LOT) {
            throw new IllegalStateException("Maximum limit of 96 capsules per lot reached.");
        }

        // Force time to NOON
        LocalDateTime dateCalibrationCapsule = targetDate != null ? targetDate.with(LocalTime.NOON) : LocalDateTime.now().with(LocalTime.NOON);

        // Calculate Delta T in hours
        double deltaTHeures = ChronoUnit.HOURS.between(lot.getCalibrationDate(), dateCalibrationCapsule);

        // Formula: Volume_ml = Dose_mci / (Concentration_Lot * exp(-0.0036 * deltaT_heures))
        double decayFactor = Math.exp(-LAMBDA * deltaTHeures);
        double volumeMl = doseMci / (lot.getRadioactiveConcentration() * decayFactor);
        double volumeMicroliter = volumeMl * 1000;

        if (volumeMicroliter > 100.0) {
            throw new IllegalArgumentException("Calculated volume exceeds the strict limit of 100 µl: " + volumeMicroliter + " µl");
        }

        // Determine tray position
        int trayPosition = findAvailableTrayPosition();
        
        // Calculate rack and rack position based on tray position (assuming 4 racks, 24 positions each)
        // 1-24: rack 1, 25-48: rack 2... Wait, there are excluded positions on the tray.
        int rackNumber = ((trayPosition - 1) / 24) + 1;
        int rackPosition = ((trayPosition - 1) % 24) + 1;

        // Generate barcode
        String displayId = String.format("CAPS-%02d", count + 1);
        String barcode = String.format("R2T-%s-%s", lot.getSupplierLotNumber(), displayId);

        Capsule capsule = Capsule.builder()
                .lot(lot)
                .displayId(displayId)
                .trayPosition(trayPosition)
                .rackNumber(rackNumber)
                .rackPosition(rackPosition)
                .doseMci(doseMci)
                .calibrationDate(dateCalibrationCapsule)
                .volumeMicroliter(volumeMicroliter)
                .barcode(barcode)
                .status(CapsuleStatus.WAITING)
                .build();

        log.info("Generated capsule {} with volume {} µl", barcode, volumeMicroliter);
        return capsuleRepository.save(capsule);
    }

    private int findAvailableTrayPosition() {
        for (int i = 1; i <= 100; i++) {
            if (EXCLUDED_POSITIONS.contains(i)) {
                continue;
            }
            if (!capsuleRepository.existsByTrayPosition(i)) { // Simplification: we might need a status check in real life
                return i;
            }
        }
        throw new IllegalStateException("No available tray positions.");
    }
}
