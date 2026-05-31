package com.neocaps.api.controller;

import com.neocaps.api.model.dto.RobotActionResponse;
import com.neocaps.api.model.dto.RobotSystemStatusResponse;
import com.neocaps.api.service.RobotActionService;
import com.neocaps.api.service.RobotStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/robot")
@RequiredArgsConstructor
@CrossOrigin
public class RobotController {

    private final RobotActionService robotActionService;
    private final RobotStatusService robotStatusService;

    @GetMapping("/actions")
    public ResponseEntity<List<RobotActionResponse>> getRobotActions() {
        return ResponseEntity.ok(robotActionService.getAllActions());
    }

    @GetMapping("/status")
    public ResponseEntity<RobotSystemStatusResponse> getSystemStatus() {
        return ResponseEntity.ok(robotStatusService.getSystemStatus());
    }
}
