package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.JwtResponse;
import com.compliance.dashboard.dto.LoginRequest;
import com.compliance.dashboard.dto.SignupRequest;
import com.compliance.dashboard.dto.UserResponse;
import com.compliance.dashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @Operation(summary = "Create a new user account")
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login and receive a JWT")
    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
