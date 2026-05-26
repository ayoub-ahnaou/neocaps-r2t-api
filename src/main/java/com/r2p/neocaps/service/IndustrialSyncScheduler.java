package com.r2p.neocaps.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndustrialSyncScheduler {

    private final PLCService plcService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 1000)
    public void syncIndustrialData() {
        plcService.readSystemStatus();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("robot", plcService.getRobotStatus());
        payload.put("pump", plcService.getDosingPumpStatus());
        
        messagingTemplate.convertAndSend("/topic/industrial-sync", (Object) payload);
    }
}
