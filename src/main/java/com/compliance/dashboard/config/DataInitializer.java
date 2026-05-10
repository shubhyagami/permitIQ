package com.compliance.dashboard.config;

import com.compliance.dashboard.entity.Gender;
import com.compliance.dashboard.entity.Role;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase("admin@test.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .age(25)
                    .gender(Gender.PREFER_NOT_TO_SAY)
                    .build();
            userRepository.save(admin);
        }
    }
}