package com.compliance.dashboard.timer;

import com.compliance.dashboard.config.ApplicationProperties;
import com.compliance.dashboard.dto.CountdownResponse;
import com.compliance.dashboard.entity.DocumentStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CountdownServiceImpl implements CountdownService {

    private final int expiringSoonDays;

    public CountdownServiceImpl(ApplicationProperties properties) {
        this.expiringSoonDays = properties.reminder().expiringSoonDays();
    }

    @Override
    public CountdownResponse calculate(LocalDate expiryDate) {
        if (expiryDate == null) {
            return CountdownResponse.builder()
                    .remainingDays(0)
                    .remainingHours(0)
                    .remainingMinutes(0)
                    .expired(false)
                    .build();
        }
        LocalDateTime expiryEnd = expiryDate.plusDays(1).atStartOfDay();
        Duration duration = Duration.between(LocalDateTime.now(), expiryEnd);
        if (duration.isNegative() || duration.isZero()) {
            return CountdownResponse.builder()
                    .remainingDays(0)
                    .remainingHours(0)
                    .remainingMinutes(0)
                    .expired(true)
                    .build();
        }
        return CountdownResponse.builder()
                .remainingDays(duration.toDays())
                .remainingHours(duration.toHoursPart())
                .remainingMinutes(duration.toMinutesPart())
                .expired(false)
                .build();
    }

    @Override
    public DocumentStatus statusFor(LocalDate expiryDate) {
        if (expiryDate == null) {
            return DocumentStatus.ACTIVE;
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return DocumentStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(expiringSoonDays))) {
            return DocumentStatus.EXPIRING_SOON;
        }
        return DocumentStatus.ACTIVE;
    }
}
