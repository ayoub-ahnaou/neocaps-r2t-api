package com.r2p.neocaps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PLCConfig {
    private String ipAddress;
    private int rack;
    private int slot;
    private String connectionMode; // SIM / REAL
    private boolean isActive;
}
