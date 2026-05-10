package com.compliance.dashboard.dto;

import com.compliance.dashboard.entity.DocumentStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DocumentResponse(
        Long id,
        String documentName,
        String documentType,
        String permitNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        String authorityName,
        String originalFileName,
        LocalDateTime uploadTime,
        DocumentStatus status,
        long remainingDays,
        long remainingHours,
        long remainingMinutes,
        boolean expired
) {
}
