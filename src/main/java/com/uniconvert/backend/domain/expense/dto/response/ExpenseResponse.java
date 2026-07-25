package com.uniconvert.backend.domain.expense.dto.response;

import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.expense.entity.RateSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal originalAmount,
        String originalCurrency,
        BigDecimal appliedRate,
        RateSource rateSource,
        LocalDate rateDate,
        BigDecimal convertedAmountHome,
        String merchantName,
        String memo,
        Long categoryId,
        Long potId,
        LocalDateTime spentAt
) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getOriginalAmount(),
                e.getOriginalCurrency(),
                e.getAppliedRate(),
                e.getRateSource(),
                e.getRateDate(),
                e.getConvertedAmountHome(),
                e.getMerchantName(),
                e.getMemo(),
                e.getCategoryId(),
                e.getPotId(),
                e.getSpentAt()
        );
    }
}