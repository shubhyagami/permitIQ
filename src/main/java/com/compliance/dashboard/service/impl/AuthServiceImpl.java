package com.compliance.dashboard.service.impl;

import com.compliance.dashboard.dto.JwtResponse;
import com.compliance.dashboard.dto.LoginRequest;
import com.compliance.dashboard.dto.SignupRequest;
import com.compliance.dashboard.dto.UserResponse;
import com.compliance.dashboard.entity.Role;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.exception.BadRequestException;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.security.JwtTokenProvider;
import com.compliance.dashboard.security.UserPrincipal;
import com.compliance.dashboard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponse register(SignupRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .company(request.getCompany())
                .age(request.getAge())
                .gender(request.getGender())
                .role(Role.ROLE_USER)
                .build();
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return JwtResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtTokenProvider.generateToken(principal))
                .userId(principal.id())
                .name(principal.name())
                .email(principal.email())
                .role(principal.role())
                .build();
    }

    private UserResponse toUserResponse(User user) {
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
