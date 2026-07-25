package com.uniconvert.backend.domain.expense.dto.response;

import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseListItemResponse(
        Long id,
        String merchantName,
        Long categoryId,
        String categoryName,
        String iconKey,
        BigDecimal convertedAmountHome,
        String originalCurrency,
        BigDecimal originalAmount,
        LocalDateTime spentAt
) {
    public static ExpenseListItemResponse from(Expense e) {
        CategoryType category = CategoryType.fromId(e.getCategoryId());
        return new ExpenseListItemResponse(
                e.getId(),
                e.getMerchantName(),
                e.getCategoryId(),
                category != null ? category.getDisplayName() : null,
                category != null ? category.getIconKey() : null,
                e.getConvertedAmountHome(),
                e.getOriginalCurrency(),
                e.getOriginalAmount(),
                e.getSpentAt()
        );
    }
}