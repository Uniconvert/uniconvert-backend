package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;

public record ReportCategoryItem(
        Long categoryId,
        String categoryName,
        String iconKey,
        BigDecimal amount,
        BigDecimal percentage
) {
}