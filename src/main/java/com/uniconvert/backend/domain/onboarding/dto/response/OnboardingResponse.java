package com.uniconvert.backend.domain.onboarding.dto.response;

import com.uniconvert.backend.domain.budget.entity.Budget;
import com.uniconvert.backend.domain.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OnboardingResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageKey,
        String primaryGoal,
        String homeCurrencyCode,
        String localCurrencyCode,
        String timezone,
        Integer onboardingStep,
        boolean onboardingCompleted,
        LocalDateTime onboardingCompletedAt,
        String yearMonth,
        BigDecimal monthlyLimitHome
) {
    public static OnboardingResponse of(User user, Budget budget) {
        return new OnboardingResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageKey(),
                user.getPrimaryGoal(),
                user.getHomeCurrencyCode(),
                user.getLocalCurrencyCode(),
                user.getTimezone(),
                user.getOnboardingStep(),
                user.isOnboardingCompleted(),
                user.getOnboardingCompletedAt(),
                budget != null ? budget.getYearMonth() : null,
                budget != null ? budget.getMonthlyLimitHome() : null
        );
    }
}