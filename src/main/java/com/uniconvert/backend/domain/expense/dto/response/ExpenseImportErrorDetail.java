package com.uniconvert.backend.domain.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExpenseImportErrorDetail(
        @Schema(description = "CSV 데이터 행 번호(헤더 제외, 1부터 시작)", example = "12")
        int rowNumber,

        @Schema(description = "가맹점명(있는 경우)", example = "Mindwise Ai LLC")
        String merchantName,

        @Schema(description = "실패 사유 — 환율을 구하지 못해 저장하지 않고 오류로 분류한 이유", example = "지원하지 않는 통화입니다: GBP")
        String reason
) {
}
