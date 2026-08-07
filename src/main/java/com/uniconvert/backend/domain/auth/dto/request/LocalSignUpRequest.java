package com.uniconvert.backend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LocalSignUpRequest(

        @Email(message = "{validation.email.invalid}")
        @NotBlank(message = "{validation.email.required}")
        String email,

        @NotBlank(message = "{validation.password.required}")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "{validation.password.policy}"
        )
        String password,

        @NotBlank(message = "{validation.nickname.required}")
        @Size(max = 20, message = "{validation.nickname.max20}")
        String nickname
) {
}
