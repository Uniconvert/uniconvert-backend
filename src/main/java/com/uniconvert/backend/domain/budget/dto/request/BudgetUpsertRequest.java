package com.uniconvert.backend.domain.budget.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotNull BigDecimal monthlyLimitHome
) {
}