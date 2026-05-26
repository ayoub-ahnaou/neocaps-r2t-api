package com.r2p.neocaps.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CapsuleRequest {
    private Double doseMci;
    private LocalDateTime targetDate;
}
