package com.uniconvert.backend.domain.pot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Pot 수정 요청")
public record PotUpdateRequest(

        @Schema(
                description = "변경할 Pot 이름. 변경하지 않을 경우 생략합니다.",
                example = "유럽 여행 경비"
        )
        @Size(
                min = 1,
                max = 255,
                message = "Pot 이름은 1자 이상 255자 이하여야 합니다."
        )
        String name,

        @Schema(
                description = "변경할 목표 대표 카테고리. 변경하지 않을 경우 생략합니다.",
                example = "TRAVEL"
        )
        @Size(
                max = 30,
                message = "목표 카테고리는 30자를 초과할 수 없습니다."
        )
        String goalCategory,

        @Schema(
                description = "변경할 최종 목표 금액. 0보다 큰 금액만 입력할 수 있습니다.",
                example = "3500000"
        )
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "목표 금액은 0보다 커야 합니다."
        )
        BigDecimal targetAmount,

        @Schema(
                description = "변경할 월 저축 계획 금액. 0원 이상 입력할 수 있습니다.",
                example = "350000"
        )
        @DecimalMin(
                value = "0.0",
                message = "월 배정 예정 금액은 0 이상이어야 합니다."
        )
        BigDecimal monthlyAllocation,

        @Schema(
                description = "변경할 화면 표시 순서. 0 이상의 정수를 입력합니다.",
                example = "1"
        )
        @PositiveOrZero(message = "표시 순서는 0 이상이어야 합니다.")
        Long displayOrder
) {
}