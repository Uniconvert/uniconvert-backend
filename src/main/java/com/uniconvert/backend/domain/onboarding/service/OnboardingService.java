package com.uniconvert.backend.domain.onboarding.service;

import com.uniconvert.backend.domain.budget.entity.Budget;
import com.uniconvert.backend.domain.budget.repository.BudgetRepository;
import com.uniconvert.backend.domain.onboarding.dto.request.OnboardingSaveRequest;
import com.uniconvert.backend.domain.onboarding.dto.response.OnboardingResponse;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public OnboardingResponse saveOnboarding(Long userId, OnboardingSaveRequest request) {
        User user = findUser(userId);

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            user.updateImageUrl(request.imageUrl());
        }

        user.updateOnboardingInfo(
                request.homeCurrencyCode().toUpperCase(),
                request.localCurrencyCode().toUpperCase(),
                request.timezone()
        );

        user.completeOnboarding();

        String yearMonth = YearMonth.now(ZoneId.of(request.timezone())).toString();

        Budget budget = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseGet(() -> new Budget(
                        user,
                        yearMonth,
                        request.monthlyLimitHome()
                ));

        budget.updateMonthlyLimitHome(request.monthlyLimitHome());
        budgetRepository.save(budget);

        return OnboardingResponse.of(user, budget);
    }

    @Transactional(readOnly = true)
    public OnboardingResponse getMyOnboarding(Long userId) {
        User user = findUser(userId);

        String timezone = user.getTimezone() != null ? user.getTimezone() : "Asia/Seoul";
        String yearMonth = YearMonth.now(ZoneId.of(timezone)).toString();

        Budget budget = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("이번 달 예산 정보가 존재하지 않습니다."));

        return OnboardingResponse.of(user, budget);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}