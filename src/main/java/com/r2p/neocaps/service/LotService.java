package com.r2p.neocaps.service;

import com.r2p.neocaps.aop.Auditable;
import com.r2p.neocaps.entity.Lot;
import com.r2p.neocaps.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LotService {

    private final LotRepository lotRepository;

    @Transactional
    @Auditable(action = "CREATE_LOT", description = "A new lot was created")
    public Lot createLot(Lot lot) {
        System.out.println("lot = " + lot);
        // Force dates to 12:00:00 (Midi)
        if (lot.getCalibrationDate() != null) {
            lot.setCalibrationDate(lot.getCalibrationDate().with(LocalTime.NOON));
        }
        log.info("Creating Lot: {}", lot.getSupplierLotNumber());
        return lotRepository.save(lot);
    }

    public List<Lot> getAllLots() {
        return lotRepository.findAll();
    }

    public Lot getLotById(UUID lotId) {
        return lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));
    }
}
