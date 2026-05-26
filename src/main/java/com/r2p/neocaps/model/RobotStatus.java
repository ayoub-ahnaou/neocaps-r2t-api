package com.r2p.neocaps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotStatus {
    private boolean ready;
    private boolean running;
    private boolean paused;
    private boolean error;
    private boolean inCycle;
    private boolean gripOn;
    private boolean gripOff;
}
