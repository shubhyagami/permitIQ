package com.compliance.dashboard.service;

import com.compliance.dashboard.dto.JwtResponse;
import com.compliance.dashboard.dto.LoginRequest;
import com.compliance.dashboard.dto.SignupRequest;
import com.compliance.dashboard.dto.UserResponse;

public interface AuthService {

    UserResponse register(SignupRequest request);

    JwtResponse login(LoginRequest request);
}
