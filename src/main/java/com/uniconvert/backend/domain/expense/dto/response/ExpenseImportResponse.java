package com.uniconvert.backend.domain.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ExpenseImportResponse(
        @Schema(description = "자동 판별된 CSV 제공자", example = "WISE")
        String provider,

        @Schema(description = "CSV 데이터 행 총 개수(헤더 제외)", example = "42")
        int totalRowCount,

        @Schema(description = "지출로 저장된 행 수", example = "30")
        int savedCount,

        @Schema(description = "입금·충전·취소 등 지출이 아니라서 제외된 행 수", example = "6")
        int excludedCount,

        @Schema(description = "환율을 구하지 못해 저장하지 않은 행 수", example = "1")
        int errorCount,

        @Schema(description = "오류로 분류된 행의 상세 사유 목록")
        List<ExpenseImportErrorDetail> errors
) {
}
