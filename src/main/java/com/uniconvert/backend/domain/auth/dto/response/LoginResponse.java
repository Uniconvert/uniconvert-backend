package com.uniconvert.backend.domain.auth.dto.response;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String accessToken,
        String refreshToken
) {
}
