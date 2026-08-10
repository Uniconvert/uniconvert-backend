package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;

public record MonthlyAmount(
        String month,
        BigDecimal amount
) {
}