package com.neocaps.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotResponse {
    private UUID id;
    private String productName;
    private String supplierLotNumber;
    private Double totalActivityMci;
    private Double radioactiveConcentration;
    private Double reservoirVolumeMicroliter;
    private LocalDate manufacturingDate;
    private LocalDate calibrationDate;
    private Double remainingVolumeMicroliter;
    private Long capsuleCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
