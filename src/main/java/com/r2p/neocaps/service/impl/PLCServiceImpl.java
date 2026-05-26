package com.r2p.neocaps.service.impl;

import com.r2p.neocaps.model.DosingPump;
import com.r2p.neocaps.model.RobotStatus;
import com.r2p.neocaps.service.PLCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PLCServiceImpl implements PLCService {

    private final RobotStatus mockRobotStatus = RobotStatus.builder()
            .ready(true)
            .running(false)
            .paused(false)
            .error(false)
            .inCycle(false)
            .gripOn(false)
            .gripOff(true)
            .build();

    private final DosingPump mockPumpStatus = DosingPump.builder()
            .portCom("COM1")
            .baudRate(9600)
            .pumpAddress(1)
            .targetVolume(0.0)
            .actualVolume(0.0)
            .isRunning(false)
            .build();

    @Override
    public void writeProductionData(int position, double volume) {
        log.info("S7 PLC Write: Writing Production Data to DB5/DB8 - Position: {}, Volume: {}", position, volume);
        // Simulate PLC writing
        mockPumpStatus.setTargetVolume(volume);
    }

    @Override
    public void triggerStartPulse() {
        log.info("S7 PLC Write: Triggering Start Pulse on %M100.0");
        // Simulate PLC trigger
        mockRobotStatus.setRunning(true);
        mockRobotStatus.setReady(false);
    }

    @Override
    public RobotStatus getRobotStatus() {
        return mockRobotStatus;
    }

    @Override
    public DosingPump getDosingPumpStatus() {
        return mockPumpStatus;
    }

    @Override
    public void readSystemStatus() {
        log.debug("S7 PLC Read: Reading System Status (DB3, DB4, DB7)");
        // In reality, we'd read from PLC here. For mock, we just use the current mock state.
    }
}
