package com.neocaps.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlcMessageDto {
    private String type; // ROBOT_STATUS, CAPSULE_MOVEMENT, PLC_ERROR, PUMP_STATUS

    // Robot status fields
    private Boolean ready;
    private Boolean running;
    private Boolean paused;
    private Boolean error;
    private Boolean inCycle;
    private Boolean gripOn;
    private Boolean gripOff;

    // Capsule movement fields
    private String capsuleBarcode;
    private String from;
    private String to;
    private LocalDateTime timestamp;

    // Dosing pump fields
    private Double targetVolume;
    private Double actualVolume;

    // PLC Error fields
    private String code;
    private String message;
}
