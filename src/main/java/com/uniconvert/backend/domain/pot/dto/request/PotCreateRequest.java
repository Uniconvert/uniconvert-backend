package com.uniconvert.backend.domain.pot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PotCreateRequest(

        @Schema(
                description = "Pot 이름",
                example = "유럽 여행"
        )
        @NotBlank(message = "{validation.pot.name.required}")
        @Size(
                max = 30,
                message = "{validation.pot.name.max30}"
        )
        String name,

        @Schema(
                description = "목표 대표 카테고리",
                example = "TRAVEL"
        )
        @Size(
                max = 30,
                message = "{validation.pot.goal_category.max30}"
        )
        String goalCategory,

        @Schema(
                description = "Pot의 최종 목표 금액",
                example = "3000000"
        )
        @NotNull(message = "{validation.pot.target.required}")
        @PositiveOrZero(message = "{validation.pot.target.nonnegative}")
        BigDecimal targetAmount,

        @Schema(
                description = "매월 Pot에 저축할 계획 금액",
                example = "300000"
        )
        @NotNull(message = "{validation.pot.monthly.required}")
        @PositiveOrZero(message = "{validation.pot.monthly.nonnegative}")
        BigDecimal monthlyAllocation,

        @Schema(
                description = "화면 표시 순서. 미입력 시 서버에서 마지막 순서로 배치합니다.",
                example = "1"
        )
        Long displayOrder
) {
}