package com.neocaps.api.controller;

import com.neocaps.api.model.dto.LotCreateRequest;
import com.neocaps.api.model.dto.LotResponse;
import com.neocaps.api.service.LotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
@CrossOrigin
public class LotController {

    private final LotService lotService;

    @PostMapping
    public ResponseEntity<LotResponse> createLot(@Valid @RequestBody LotCreateRequest request) {
        LotResponse response = lotService.createLot(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LotResponse>> getAllLots() {
        return ResponseEntity.ok(lotService.getAllLots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotResponse> getLotById(@PathVariable UUID id) {
        return ResponseEntity.ok(lotService.getLotById(id));
    }

    @GetMapping("/supplier/{supplierLotNumber}")
    public ResponseEntity<LotResponse> getLotBySupplierNumber(@PathVariable String supplierLotNumber) {
        return ResponseEntity.ok(lotService.getLotBySupplierNumber(supplierLotNumber));
    }
}
