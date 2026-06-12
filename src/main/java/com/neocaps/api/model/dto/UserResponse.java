package com.neocaps.api.model.dto;

import com.neocaps.api.enums.UserRole;
import com.neocaps.api.enums.UserStatus;
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
public class UserResponse {
    private UUID id;
    private String username;
    private UserRole role;
    private LocalDateTime lastLogin;
    private UserStatus status;
}
