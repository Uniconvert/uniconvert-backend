package com.uniconvert.backend.domain.pot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PotAllocationUpsertRequest(

        @NotBlank(message = "연월은 필수입니다.")
        @Pattern(
                regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
                message = "연월은 YYYY-MM 형식이어야 합니다."
        )
        String yearMonth,

        @NotNull(message = "배정 금액은 필수입니다.")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "배정 금액은 0보다 커야 합니다."
        )
        BigDecimal amount
) {
}
