package com.neocaps.api.service;

import com.neocaps.api.enums.CapsuleStatus;
import com.neocaps.api.enums.RobotActionType;
import com.neocaps.api.model.dto.CapsuleResponse;
import com.neocaps.api.model.dto.PlcMessageDto;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.runtime.DosingPump;
import com.neocaps.api.model.runtime.RobotStatus;
import com.neocaps.api.repository.CapsuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlcEventHandlerService {

    private final CapsuleRepository capsuleRepository;
    private final RobotActionService robotActionService;
    private final RobotStatusService robotStatusService;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Safely processes an incoming message from the PLC, updates domain data/runtime state,
     * persists traceability logs, and broadcasts updates via WebSockets.
     */
    @Transactional
    public void handlePlcMessage(PlcMessageDto message) {
        if (message == null || message.getType() == null) {
            log.warn("Received empty or invalid PLC message.");
            return;
        }

        try {
            switch (message.getType().toUpperCase()) {
                case "ROBOT_STATUS":
                    handleRobotStatusUpdate(message);
                    break;
                case "CAPSULE_MOVEMENT":
                    handleCapsuleMovement(message);
                    break;
                case "PLC_ERROR":
                    handlePlcError(message);
                    break;
                case "PUMP_STATUS":
                    handlePumpStatusUpdate(message);
                    break;
                default:
                    log.warn("Unknown PLC message type: {}", message.getType());
            }
        } catch (Exception e) {
            log.error("Error processing PLC message of type {}: {}", message.getType(), e.getMessage(), e);
        }
    }

    private void handleRobotStatusUpdate(PlcMessageDto message) {
        RobotStatus status = RobotStatus.builder()
                .ready(Boolean.TRUE.equals(message.getReady()))
                .running(Boolean.TRUE.equals(message.getRunning()))
                .paused(Boolean.TRUE.equals(message.getPaused()))
                .error(Boolean.TRUE.equals(message.getError()))
                .inCycle(Boolean.TRUE.equals(message.getInCycle()))
                .gripOn(Boolean.TRUE.equals(message.getGripOn()))
                .gripOff(Boolean.TRUE.equals(message.getGripOff()))
                .build();

        robotStatusService.updateRobotStatus(status);

        // Broadcast updated system status to frontend client topic
        messagingTemplate.convertAndSend("/topic/robot-status", robotStatusService.getSystemStatus());
        log.debug("Processed robot status update from PLC. System active: {}", status.isRunning());
    }

    private void handleCapsuleMovement(PlcMessageDto message) {
        if (message.getCapsuleBarcode() == null) {
            log.warn("Capsule movement event missing capsuleBarcode.");
            return;
        }

        Capsule capsule = capsuleRepository.findByBarcode(message.getCapsuleBarcode()).orElse(null);
        if (capsule == null) {
            log.warn("Capsule with barcode '{}' not found in database. Ignoring event.", message.getCapsuleBarcode());
            return;
        }

        String toPos = message.getTo() != null ? message.getTo().toUpperCase() : "";
        String fromPos = message.getFrom() != null ? message.getFrom().toUpperCase() : "";

        RobotActionType actionType = RobotActionType.MOVE;
        String desc = String.format("Capsule %s moved from %s to %s", capsule.getDisplayId(), fromPos, toPos);

        // Update capsule domain status based on movement destination
        if (toPos.contains("FILLING")) {
            capsule.setStatus(CapsuleStatus.FILLED);
            actionType = RobotActionType.FILL;
            desc = String.format("Capsule %s filled at dosing station", capsule.getDisplayId());
            auditLogService.log("CAPSULE_FILL", desc);
        } else if (toPos.contains("PACKAGE") || toPos.contains("BOTTLE")) {
            capsule.setStatus(CapsuleStatus.PACKAGED);
            actionType = RobotActionType.PACKAGE;
            desc = String.format("Capsule %s packaged in container", capsule.getDisplayId());
            auditLogService.log("CAPSULE_PACKAGE", desc);
        } else if (toPos.contains("RACK") || toPos.contains("STORE")) {
            capsule.setStatus(CapsuleStatus.STORED);
            actionType = RobotActionType.STORE;
            desc = String.format("Capsule %s stored in Tray Position %d", capsule.getDisplayId(), capsule.getTrayPosition());
            auditLogService.log("CAPSULE_STORAGE", desc);
        } else if (toPos.contains("PICK")) {
            actionType = RobotActionType.PICK;
        }

        capsuleRepository.save(capsule);

        // Save robot action traceability history
        robotActionService.saveAction(capsule, actionType, fromPos, toPos, true, desc);

        // Broadcast capsule event update
        CapsuleResponse capsuleResponse = CapsuleResponse.builder()
                .id(capsule.getId())
                .displayId(capsule.getDisplayId())
                .trayPosition(capsule.getTrayPosition())
                .doseMci(capsule.getDoseMci())
                .volumeMicroliter(capsule.getVolumeMicroliter())
                .status(capsule.getStatus())
                .lotId(capsule.getLot().getId())
                .supplierLotNumber(capsule.getLot().getSupplierLotNumber())
                .build();

        messagingTemplate.convertAndSend("/topic/capsule-events", capsuleResponse);
        log.info("Capsule movement processed: {}", desc);
    }

    private void handlePlcError(PlcMessageDto message) {
        String errCode = message.getCode() != null ? message.getCode() : "PLC_ERROR";
        String errMsg = message.getMessage() != null ? message.getMessage() : "An error occurred on the PLC.";

        Capsule capsule = null;
        if (message.getCapsuleBarcode() != null) {
            capsule = capsuleRepository.findByBarcode(message.getCapsuleBarcode()).orElse(null);
            if (capsule != null) {
                capsule.setStatus(CapsuleStatus.ERROR);
                capsuleRepository.save(capsule);
            }
        }

        // Persist error action log
        robotActionService.saveAction(
                capsule,
                RobotActionType.ERROR,
                message.getFrom(),
                message.getTo(),
                false,
                String.format("Code: %s, Msg: %s", errCode, errMsg)
        );

        auditLogService.log("PLC_ERROR", String.format("Error [%s]: %s", errCode, errMsg));

        // Broadcast error notification to clients
        messagingTemplate.convertAndSend("/topic/errors", message);
        log.error("PLC error received: Code={}, Msg={}", errCode, errMsg);
    }

    private void handlePumpStatusUpdate(PlcMessageDto message) {
        DosingPump pump = robotStatusService.getDosingPump();
        if (message.getTargetVolume() != null) {
            pump.setTargetVolume(message.getTargetVolume());
        }
        if (message.getActualVolume() != null) {
            pump.setActualVolume(message.getActualVolume());
        }
        if (message.getRunning() != null) {
            pump.setRunning(message.getRunning());
        }

        robotStatusService.updateDosingPump(pump);

        // Broadcast updated status
        messagingTemplate.convertAndSend("/topic/robot-status", robotStatusService.getSystemStatus());
        log.debug("Processed pump status update. Pump running: {}", pump.isRunning());
    }
}
