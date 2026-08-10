package com.uniconvert.backend.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "월별 지출 리포트 응답")
public record ReportMonthlyResponse(

        @Schema(description = "조회 범위 전체 월의 지출 합계", example = "5400000")
        BigDecimal totalAmount,

        @Schema(description = "월별 지출 합계 리스트 (지출 없는 달은 0으로 채움)")
        List<MonthlyAmount> monthlyAmounts,

        @Schema(
                description = """
                        조회 범위 내 마지막 달 지출의 전월 대비 변동률입니다.
                        양수이면 증가, 음수이면 감소이며,
                        전월 지출이 0원이거나 범위에 전월이 포함되지 않으면 null입니다.
                        """,
                example = "-5.20"
        )
        BigDecimal changeRate,

        @Schema(description = "전월 대비 변동률의 비교 기준월(yyyy-MM). 비교할 수 없으면 null", example = "2026-06")
        String comparedMonth
) {
}