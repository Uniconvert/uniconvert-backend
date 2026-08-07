package com.uniconvert.backend.domain.pot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Pot 월별 실제 배정 금액 생성·수정 요청")
public record PotAllocationUpsertRequest(

        @Schema(
                description = """
                        금액을 배정할 월입니다.
                        YYYY-MM 형식으로 입력합니다.
                        동일한 Pot과 월의 배정 내역이 이미 있으면 기존 금액을 변경합니다.
                        """,
                example = "2026-08",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{validation.pot.allocation.month.required}")
        @Pattern(
                regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
                message = "{validation.pot.allocation.month.format}"
        )
        String yearMonth,

        @Schema(
                description = """
                        해당 월에 Pot에 실제로 배정할 금액입니다.
                        기존 금액에 더하는 방식이 아니라 입력한 금액으로 저장됩니다.
                        0원을 입력하면 해당 월의 배정 금액이 0원으로 변경됩니다.
                        """,
                example = "300000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{validation.pot.allocation.amount.required}")
        @PositiveOrZero(message = "{validation.pot.allocation.amount.nonnegative}")
        BigDecimal amount
) {
}