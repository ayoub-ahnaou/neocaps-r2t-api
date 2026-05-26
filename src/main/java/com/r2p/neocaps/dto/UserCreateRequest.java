package com.r2p.neocaps.dto;

import com.r2p.neocaps.entity.enums.UserRole;
import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private UserRole role;
}
