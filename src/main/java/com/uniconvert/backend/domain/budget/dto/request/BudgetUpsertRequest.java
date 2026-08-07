package com.uniconvert.backend.domain.budget.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotNull(message = "{validation.budget.required}")
        @PositiveOrZero(message = "{validation.budget.nonnegative}")
        BigDecimal monthlyLimitHome
) {
}