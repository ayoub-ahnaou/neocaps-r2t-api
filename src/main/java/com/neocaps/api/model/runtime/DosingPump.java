package com.neocaps.api.model.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DosingPump {
    private String portCom;
    private int baudRate;
    private int pumpAddress;
    private double targetVolume;
    private double actualVolume;
    private boolean running;
}
