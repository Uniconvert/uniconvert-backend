package com.uniconvert.backend.domain.auth.dto.response;

public record EmailVerificationConfirmResponse(
        boolean isEmailVerified
) {
}
