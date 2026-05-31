package com.neocaps.api.model.dto;

import com.neocaps.api.model.runtime.DosingPump;
import com.neocaps.api.model.runtime.PLCConfig;
import com.neocaps.api.model.runtime.RobotStatus;
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
public class RobotSystemStatusResponse {
    private PLCConfig plcConfig;
    private RobotStatus robotStatus;
    private DosingPump dosingPump;
}
