package com.uniconvert.backend.domain.budget.dto.response;

import com.uniconvert.backend.domain.budget.entity.Budget;

import java.math.BigDecimal;

public record BudgetResponse(
        Long budgetId,
        String yearMonth,
        BigDecimal monthlyLimitHome
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getYearMonth(),
                budget.getMonthlyLimitHome()
        );
    }
}