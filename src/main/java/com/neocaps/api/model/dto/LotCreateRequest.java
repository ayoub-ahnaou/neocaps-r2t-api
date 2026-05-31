package com.neocaps.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class LotCreateRequest {

    @NotBlank(message = "Supplier lot number is required")
    private String supplierLotNumber;

    @NotNull(message = "Total activity is required")
    @Positive(message = "Total activity must be positive")
    private Double totalActivityMci;

    @NotNull(message = "Radioactive concentration is required")
    @Positive(message = "Radioactive concentration must be positive")
    private Double radioactiveConcentration; // mCi/µL

    @NotNull(message = "Reservoir volume is required")
    @Positive(message = "Reservoir volume must be positive")
    private Double reservoirVolumeMicroliter;

    @NotNull(message = "Manufacturing date is required")
    private LocalDateTime manufacturingDate;

    @NotNull(message = "Calibration date is required")
    private LocalDateTime calibrationDate;
}
