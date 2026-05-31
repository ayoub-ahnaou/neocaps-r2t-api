package com.neocaps.api.model.runtime;

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
public class PLCConfig {
    private String ipAddress;
    private int rack;
    private int slot;
    private String connectionMode;
    private boolean active;
}
