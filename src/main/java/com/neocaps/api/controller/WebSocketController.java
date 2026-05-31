package com.neocaps.api.controller;

import com.neocaps.api.model.dto.PlcMessageDto;
import com.neocaps.api.service.PlcEventHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final PlcEventHandlerService plcEventHandlerService;

    /**
     * Receives event-driven STOMP messages from the PLC.
     * Route: /app/plc-event
     */
    @MessageMapping("/plc-event")
    public void receivePlcEvent(@Payload PlcMessageDto message) {
        log.info("Received PLC WebSocket event. Type: {}", message.getType());
        plcEventHandlerService.handlePlcMessage(message);
    }
}
