package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
        BigDecimal totalAmount,
        List<DailyAmount> dailyAmounts,
        BigDecimal changeRate,      // 오늘 대비 어제 지출 변동률(%). 비교 불가 시 null
        LocalDate comparedDate      // 비교 기준일(어제). 비교 불가 시 null
) {
}