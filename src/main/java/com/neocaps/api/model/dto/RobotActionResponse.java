package com.neocaps.api.model.dto;

import com.neocaps.api.enums.RobotActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotActionResponse {
    private UUID id;
    private UUID capsuleId;
    private String capsuleDisplayId;
    private String capsuleBarcode;
    private RobotActionType actionType;
    private String fromPosition;
    private String toPosition;
    private LocalDateTime timestamp;
    private Boolean success;
    private String message;
}
