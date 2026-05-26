package com.r2p.neocaps.service;

import com.r2p.neocaps.model.DosingPump;
import com.r2p.neocaps.model.RobotStatus;

public interface PLCService {
    void writeProductionData(int position, double volume);
    void triggerStartPulse();
    RobotStatus getRobotStatus();
    DosingPump getDosingPumpStatus();
    void readSystemStatus();
}
