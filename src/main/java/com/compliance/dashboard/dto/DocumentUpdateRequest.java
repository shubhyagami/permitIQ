package com.compliance.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentUpdateRequest {

    @NotBlank
    @Size(max = 180)
    private String documentName;

    @Size(max = 80)
    private String documentType;

    @Size(max = 120)
    private String permitNumber;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    @Size(max = 180)
    private String authorityName;
}
