package com.r2p.neocaps.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProcessCapsuleRequest {
    private UUID capsuleId;
    private String scannedBarcode;
}
