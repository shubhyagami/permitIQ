package com.compliance.dashboard.dto;

import com.compliance.dashboard.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 160)
    private String company;

    @NotNull
    @Min(13)
    @Max(120)
    private Integer age;

    @NotNull
    private Gender gender;
}
