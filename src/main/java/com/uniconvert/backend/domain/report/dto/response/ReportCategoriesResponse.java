package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportCategoriesResponse(
        BigDecimal totalAmount,
        List<ReportCategoryItem> categories
) {
}