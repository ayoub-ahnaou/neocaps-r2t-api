package com.neocaps.api.model.dto;

import com.neocaps.api.enums.CapsuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapsuleResponse {
    private UUID id;
    private String displayId;
    private Integer trayPosition;
    private Integer rackNumber;
    private Integer rackPosition;
    private Double doseMci;
    private Double volumeMicroliter;
    private String barcode;
    private LocalDateTime calibrationDate;
    private CapsuleStatus status;
    private UUID lotId;
    private String supplierLotNumber;
}
