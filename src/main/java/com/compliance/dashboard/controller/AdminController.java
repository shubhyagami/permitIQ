package com.compliance.dashboard.controller;

import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "users", userRepository.count(),
                "documents", documentRepository.count()
        );
    }
}
