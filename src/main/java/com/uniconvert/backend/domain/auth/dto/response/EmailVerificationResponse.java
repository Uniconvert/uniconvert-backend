package com.uniconvert.backend.domain.auth.dto.response;

import java.time.LocalDateTime;

public record EmailVerificationResponse(
        LocalDateTime expiresAt,
        int resendCount,
        int remainingResends
) {
}
