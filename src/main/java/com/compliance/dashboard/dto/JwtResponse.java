package com.compliance.dashboard.dto;

import com.compliance.dashboard.entity.Role;
import lombok.Builder;

@Builder
public record JwtResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String name,
        String email,
        Role role
) {
}
