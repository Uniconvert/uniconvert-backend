package com.uniconvert.backend.domain.expense.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExpenseImportErrorDetail(
        @Schema(description = "CSV 데이터 행 번호(헤더 제외, 1부터 시작)", example = "12")
        int rowNumber,

        @Schema(description = "가맹점명(있는 경우)", example = "Mindwise Ai LLC")
        String merchantName,

        @Schema(description = "실패 사유 — 요청 로케일(Accept-Language)에 맞춰 반환됩니다", example = "2026-02-26 기준 GBP 환율을 구할 수 없어 저장하지 못했어요")
        String reason
) {
}
