package com.neocaps.api.model.dto;

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
public class TrayStatusResponse {
    private Integer position;
    private Boolean usable;
    private CapsuleResponse capsule;
}
