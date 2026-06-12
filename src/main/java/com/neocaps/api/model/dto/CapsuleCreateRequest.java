package com.neocaps.api.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CapsuleCreateRequest {

    @NotNull(message = "Lot ID is required")
    private UUID lotId;

    @NotNull(message = "Requested capsule dose is required")
    @Positive(message = "Capsule dose must be positive")
    private Double doseMci;

    @NotNull(message = "Client reference is required")
    private String clientReference;

    @NotNull(message = "Manufacturing date is required")
    private LocalDate manufacturingDate;

    @NotNull(message = "Calibration date is required")
    private LocalDate calibrationDate;
}
