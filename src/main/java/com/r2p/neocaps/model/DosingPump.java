package com.r2p.neocaps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DosingPump {
    private String portCom;
    private int baudRate;
    private int pumpAddress;
    private double targetVolume;
    private double actualVolume;
    private boolean isRunning;
}
