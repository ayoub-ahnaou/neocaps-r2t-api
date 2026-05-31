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
public class RobotStatus {
    private boolean ready;
    private boolean running;
    private boolean paused;
    private boolean error;
    private boolean inCycle;
    private boolean gripOn;
    private boolean gripOff;
}
