package com.compliance.dashboard.dto;

import lombok.Builder;

@Builder
public record CountdownResponse(
        long remainingDays,
        long remainingHours,
        long remainingMinutes,
        boolean expired
) {
}
