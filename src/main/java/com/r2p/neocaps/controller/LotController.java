package com.r2p.neocaps.controller;

import com.r2p.neocaps.dto.CapsuleRequest;
import com.r2p.neocaps.entity.Capsule;
import com.r2p.neocaps.entity.Lot;
import com.r2p.neocaps.service.CapsuleService;
import com.r2p.neocaps.service.LotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotService lotService;
    private final CapsuleService capsuleService;

    @PostMapping
    public ResponseEntity<Lot> createLot(@RequestBody Lot lot) {
        System.out.println("lot = " + lot);
        return ResponseEntity.ok(lotService.createLot(lot));
    }

    @GetMapping
    public ResponseEntity<List<Lot>> getAllLots() {
        return ResponseEntity.ok(lotService.getAllLots());
    }

    @PostMapping("/{lotId}/capsules")
    public ResponseEntity<Capsule> generateCapsule(
            @PathVariable UUID lotId,
            @RequestBody CapsuleRequest request) {
        Capsule capsule = capsuleService.generateCapsule(lotId, request.getDoseMci(), request.getTargetDate());
        return ResponseEntity.ok(capsule);
    }
}
