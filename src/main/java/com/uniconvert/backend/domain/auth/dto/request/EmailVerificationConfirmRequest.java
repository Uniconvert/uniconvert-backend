package com.uniconvert.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationConfirmRequest(
        @NotBlank(message = "{validation.verification_token.required}")
        String token
) {
}
