package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.UserResponse;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final SecurityUtil securityUtil;

    @Operation(summary = "Current authenticated user")
    @GetMapping("/me")
    public UserResponse me() {
        User user = securityUtil.currentUser();
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .company(user.getCompany())
                .age(user.getAge())
                .gender(user.getGender())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
