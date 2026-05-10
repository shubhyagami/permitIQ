package com.compliance.dashboard.util;

import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.exception.ResourceNotFoundException;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userRepository.findById(userPrincipal.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        }
        throw new ResourceNotFoundException("Current user not found");
    }
}
