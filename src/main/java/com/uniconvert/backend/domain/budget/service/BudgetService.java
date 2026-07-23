package com.uniconvert.backend.domain.budget.service;

import com.uniconvert.backend.domain.budget.dto.request.BudgetUpsertRequest;
import com.uniconvert.backend.domain.budget.dto.response.BudgetResponse;
import com.uniconvert.backend.domain.budget.entity.Budget;
import com.uniconvert.backend.domain.budget.repository.BudgetRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long userId, String yearMonth) {
        Budget budget = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return BudgetResponse.from(budget);
    }

    @Transactional
    public BudgetResponse upsertBudget(Long userId, String yearMonth, BudgetUpsertRequest request) {
        Budget budget = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElse(null);

        if (budget == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            budget = new Budget(user, yearMonth, request.monthlyLimitHome());
        } else {
            budget.updateAmount(request.monthlyLimitHome());
        }

        Budget saved = budgetRepository.save(budget);
        return BudgetResponse.from(saved);
    }
}