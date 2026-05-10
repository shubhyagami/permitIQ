package com.compliance.dashboard.timer;

import com.compliance.dashboard.dto.CountdownResponse;
import com.compliance.dashboard.entity.DocumentStatus;

import java.time.LocalDate;

public interface CountdownService {

    CountdownResponse calculate(LocalDate expiryDate);

    DocumentStatus statusFor(LocalDate expiryDate);
}
