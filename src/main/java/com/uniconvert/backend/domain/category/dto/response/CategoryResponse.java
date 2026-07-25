package com.uniconvert.backend.domain.category.dto.response;

import com.uniconvert.backend.domain.category.enums.CategoryType;

public record CategoryResponse(
        Long categoryId,
        String name,
        String iconKey,
        Integer sortOrder
) {
    public static CategoryResponse from(CategoryType type) {
        return new CategoryResponse(
                type.getCategoryId(),
                type.getDisplayName(),
                type.getIconKey(),
                type.getSortOrder()
        );
    }

}
