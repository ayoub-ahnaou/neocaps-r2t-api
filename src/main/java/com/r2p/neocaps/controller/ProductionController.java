package com.r2p.neocaps.controller;

import com.r2p.neocaps.dto.ProcessCapsuleRequest;
import com.r2p.neocaps.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping("/process-capsule")
    public ResponseEntity<String> processCapsule(@RequestBody ProcessCapsuleRequest request) {
        productionService.processCapsule(request.getCapsuleId(), request.getScannedBarcode());
        return ResponseEntity.ok("Capsule process started successfully on PLC.");
    }
}
