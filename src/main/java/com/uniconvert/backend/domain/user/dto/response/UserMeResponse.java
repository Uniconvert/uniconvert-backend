package com.uniconvert.backend.domain.user.dto.response;

import com.uniconvert.backend.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserMeResponse(
        Long userId,
        String email,
        String nickname,
        String imageUrl,
        String homeCurrencyCode,
        String localCurrencyCode,
        String timezone,
        Integer onboardingStep,
        boolean onboardingCompleted,
        LocalDateTime onboardingCompletedAt
) {
    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getImageUrl(),
                user.getHomeCurrencyCode(),
                user.getLocalCurrencyCode(),
                user.getTimezone(),
                user.getOnboardingStep(),
                user.isOnboardingCompleted(),
                user.getOnboardingCompletedAt()
        );
    }
}