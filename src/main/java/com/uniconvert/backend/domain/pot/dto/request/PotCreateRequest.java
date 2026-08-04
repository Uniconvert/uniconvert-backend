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
        @NotBlank(message = "Pot 이름은 필수입니다.")
        @Size(
                max = 255,
                message = "Pot 이름은 255자 이하여야 합니다."
        )
        String name,

        @Schema(
                description = "목표 대표 카테고리",
                example = "TRAVEL"
        )
        @Size(
                max = 30,
                message = "목표 대표 카테고리는 30자 이하여야 합니다."
        )
        String goalCategory,

        @Schema(
                description = "Pot의 최종 목표 금액",
                example = "3000000"
        )
        @NotNull(message = "목표 금액은 필수입니다.")
        @PositiveOrZero(message = "목표 금액은 0원 이상이어야 합니다.")
        BigDecimal targetAmount,

        @Schema(
                description = "매월 Pot에 저축할 계획 금액",
                example = "300000"
        )
        @NotNull(message = "월 저축 계획 금액은 필수입니다.")
        @PositiveOrZero(message = "월 저축 계획 금액은 0원 이상이어야 합니다.")
        BigDecimal monthlyAllocation,

        @Schema(
                description = "화면 표시 순서. 미입력 시 서버에서 마지막 순서로 배치합니다.",
                example = "1"
        )
        Long displayOrder
) {
}