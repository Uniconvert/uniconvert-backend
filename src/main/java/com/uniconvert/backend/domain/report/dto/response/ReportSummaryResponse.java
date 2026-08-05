package com.uniconvert.backend.domain.report.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "지출 리포트 요약 응답")
public record ReportSummaryResponse(

        @Schema(
                description = "조회 기간의 전체 지출 합계",
                example = "910000"
        )
        BigDecimal totalAmount,

        @Schema(description = "조회 기간의 일별 지출 합계")
        List<DailyAmount> dailyAmounts,

        @Schema(
                description = """
                        오늘 지출의 전일 대비 변동률입니다.
                        양수이면 증가, 음수이면 감소이며,
                        어제 지출이 0원이어서 비교할 수 없으면 null입니다.
                        """,
                example = "-5.20"
        )
        BigDecimal changeRate,

        @Schema(
                description = "전일 대비 변동률의 비교 기준일. 비교할 수 없으면 null",
                example = "2026-08-04"
        )
        LocalDate comparedDate,

        @Schema(
                description = "이번 주 중 지출 금액이 가장 많았던 날짜. 지출이 없으면 null",
                example = "8/3"
        )
        String weeklyMaxDate,

        @Schema(
                description = "오늘 지출 합계",
                example = "68800"
        )
        BigDecimal todayExpense,

        @Schema(
                description = "이번 달에 가장 많은 금액을 지출한 카테고리명. 지출이 없으면 null",
                example = "학업비"
        )
        String topCategory,

        @Schema(description = "리포트 화면에 표시할 유니 캐릭터 멘트 모음")
        UniMessageBundleResponse uniMessages
) {
}