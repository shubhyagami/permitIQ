package com.compliance.dashboard.dto;

import com.compliance.dashboard.entity.Gender;
import com.compliance.dashboard.entity.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String company,
        Integer age,
        Gender gender,
        Role role,
        LocalDateTime createdAt
) {
}
