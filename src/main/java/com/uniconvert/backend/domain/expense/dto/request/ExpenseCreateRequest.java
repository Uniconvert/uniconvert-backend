package com.uniconvert.backend.domain.expense.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseCreateRequest(

        @Schema(description = "지출 금액 (원본 통화 기준)", example = "45.50")
        @NotNull @Positive
        BigDecimal originalAmount,

        @Schema(description = "원본 통화 코드", example = "USD")
        @NotBlank @Size(min = 3, max = 3)
        String originalCurrency,

        @Schema(description = "지출 일시", example = "2026-07-12T00:00:00")
        @NotNull
        LocalDateTime spentAt,

        @Schema(description = "카테고리 ID (1~9)", example = "1")
        @NotNull
        Long categoryId,

        @Schema(description = "상점명 (선택)", example = "Starbucks")
        @Size(max = 255)
        String merchantName,

        @Schema(description = "메모 (선택)", example = "친구랑 커피")
        @Size(max = 255)
        String memo,

        @Schema(description = "연결할 Pot ID (선택)", example = "3")
        Long potId
) {
}