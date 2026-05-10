package com.compliance.dashboard.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DocumentExtractionResult(
        String documentName,
        String permitNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        String authorityName,
        String documentType,
        String rawText,
        boolean extractionSuccessful,
        String failureReason
) {

    public static DocumentExtractionResult failed(String reason) {
        return DocumentExtractionResult.builder()
                .extractionSuccessful(false)
                .failureReason(reason)
                .build();
    }
}
