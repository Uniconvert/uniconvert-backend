package com.uniconvert.backend.domain.pot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PotCreateRequest(

        @NotBlank(message = "Pot 이름은 필수입니다.")
        @Size(max = 255, message = "Pot 이름은 255자를 초과할 수 없습니다.")
        String name,

        @Size(max = 30, message = "목표 카테고리는 30자를 초과할 수 없습니다.")
        String goalCategory,

        @NotNull(message = "목표 금액은 필수입니다.")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "목표 금액은 0보다 커야 합니다."
        )
        BigDecimal targetAmount,

        @NotNull(message = "월 배정 예정 금액은 필수입니다.")
        @DecimalMin(
                value = "0.0",
                message = "월 배정 예정 금액은 0 이상이어야 합니다."
        )
        BigDecimal monthlyAllocation,

        @PositiveOrZero(message = "표시 순서는 0 이상이어야 합니다.")
        Long displayOrder
) {
}