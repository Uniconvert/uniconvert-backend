package com.uniconvert.backend.domain.expense.dto.response;

import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.expense.entity.Expense;

import java.time.LocalDateTime;

public record MemoListItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String iconKey,
        String memo,
        LocalDateTime spentAt
) {
    public static MemoListItemResponse from(Expense e) {
        CategoryType category = CategoryType.fromId(e.getCategoryId());
        return new MemoListItemResponse(
                e.getId(),
                e.getCategoryId(),
                category != null ? category.getDisplayName() : null,
                category != null ? category.getIconKey() : null,
                e.getMemo(),
                e.getSpentAt()
        );
    }
}