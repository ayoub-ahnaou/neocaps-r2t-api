package com.neocaps.api.service;

import com.neocaps.api.enums.RobotActionType;
import com.neocaps.api.model.dto.RobotActionResponse;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.RobotAction;
import com.neocaps.api.repository.RobotActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RobotActionService {

    private final RobotActionRepository robotActionRepository;

    @Transactional
    public RobotAction saveAction(Capsule capsule, RobotActionType actionType, 
                                  String fromPosition, String toPosition, 
                                  boolean success, String message) {
        RobotAction action = RobotAction.builder()
                .capsule(capsule)
                .actionType(actionType)
                .fromPosition(fromPosition)
                .toPosition(toPosition)
                .timestamp(LocalDateTime.now())
                .success(success)
                .message(message)
                .build();

        return robotActionRepository.save(action);
    }

    public List<RobotActionResponse> getAllActions() {
        return robotActionRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RobotActionResponse> getActionsForCapsule(Capsule capsule) {
        return robotActionRepository.findByCapsule(capsule).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RobotActionResponse mapToResponse(RobotAction action) {
        return RobotActionResponse.builder()
                .id(action.getId())
                .capsuleId(action.getCapsule() != null ? action.getCapsule().getId() : null)
                .capsuleDisplayId(action.getCapsule() != null ? action.getCapsule().getDisplayId() : null)
                .capsuleBarcode(action.getCapsule() != null ? action.getCapsule().getBarcode() : null)
                .actionType(action.getActionType())
                .fromPosition(action.getFromPosition())
                .toPosition(action.getToPosition())
                .timestamp(action.getTimestamp())
                .success(action.getSuccess())
                .message(action.getMessage())
                .build();
    }
}
