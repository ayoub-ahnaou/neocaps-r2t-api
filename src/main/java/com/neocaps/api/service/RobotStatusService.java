package com.neocaps.api.service;

import com.neocaps.api.model.dto.RobotSystemStatusResponse;
import com.neocaps.api.model.runtime.DosingPump;
import com.neocaps.api.model.runtime.PLCConfig;
import com.neocaps.api.model.runtime.RobotStatus;
import org.springframework.stereotype.Service;

@Service
public class RobotStatusService {

    // Thread-safe volatile references to runtime hardware states
    private volatile PLCConfig plcConfig;
    private volatile RobotStatus robotStatus;
    private volatile DosingPump dosingPump;

    public RobotStatusService() {
        // Initialize with default/offline values
        this.plcConfig = PLCConfig.builder()
                .ipAddress("192.168.1.10")
                .rack(1)
                .slot(2)
                .connectionMode("TCP/IP")
                .active(false)
                .build();

        this.robotStatus = RobotStatus.builder()
                .ready(false)
                .running(false)
                .paused(false)
                .error(false)
                .inCycle(false)
                .gripOn(false)
                .gripOff(true)
                .build();

        this.dosingPump = DosingPump.builder()
                .portCom("COM3")
                .baudRate(9600)
                .pumpAddress(1)
                .targetVolume(0.0)
                .actualVolume(0.0)
                .running(false)
                .build();
    }

    public RobotSystemStatusResponse getSystemStatus() {
        return RobotSystemStatusResponse.builder()
                .plcConfig(this.plcConfig)
                .robotStatus(this.robotStatus)
                .dosingPump(this.dosingPump)
                .build();
    }

    public synchronized void updatePLCConfig(PLCConfig newConfig) {
        this.plcConfig = newConfig;
    }

    public synchronized void updateRobotStatus(RobotStatus newStatus) {
        this.robotStatus = newStatus;
    }

    public synchronized void updateDosingPump(DosingPump newPump) {
        this.dosingPump = newPump;
    }

    public PLCConfig getPlcConfig() {
        return plcConfig;
    }

    public RobotStatus getRobotStatus() {
        return robotStatus;
    }

    public DosingPump getDosingPump() {
        return dosingPump;
    }
}
