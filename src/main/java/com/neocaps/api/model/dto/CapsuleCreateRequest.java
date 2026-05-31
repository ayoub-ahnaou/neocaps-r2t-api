package com.neocaps.api.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CapsuleCreateRequest {

    @NotNull(message = "Lot ID is required")
    private UUID lotId;

    @NotNull(message = "Requested capsule dose is required")
    @Positive(message = "Capsule dose must be positive")
    private Double doseMci;

    @NotNull(message = "Target calibration date is required")
    private LocalDateTime calibrationDate;
}
