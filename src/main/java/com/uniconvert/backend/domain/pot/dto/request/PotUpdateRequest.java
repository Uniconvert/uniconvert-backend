package com.uniconvert.backend.domain.pot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
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
                max = 30,
                message = "{validation.pot.name.max30}"
        )
        @Pattern(
                regexp = ".*\\S.*",
                message = "{validation.pot.name.required}"
        )
        String name,

        @Schema(
                description = "변경할 목표 대표 카테고리. 변경하지 않을 경우 생략합니다.",
                example = "TRAVEL"
        )
        @Size(
                max = 30,
                message = "{validation.pot.goal_category.max30}"
        )
        String goalCategory,

        @Schema(
                description = "변경할 Pot 대표 이미지 key. 변경하지 않을 경우 생략합니다.",
                example = "pot_travel_01"
        )
        @Size(
                max = 100,
                message = "{validation.pot.representative_image_key.max100}"
        )
        String representativeImageKey,

        @Schema(
                description = "변경할 최종 목표 금액. 0보다 큰 금액만 입력할 수 있습니다.",
                example = "3500000"
        )
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "{validation.pot.target.positive}"
        )
        BigDecimal targetAmount,

        @Schema(
                description = "변경할 월 저축 계획 금액. 0원 이상 입력할 수 있습니다.",
                example = "350000"
        )
        @DecimalMin(
                value = "0.0",
                message = "{validation.pot.monthly.nonnegative}"
        )
        BigDecimal monthlyAllocation,

        @Schema(
                description = "변경할 화면 표시 순서. 0 이상의 정수를 입력합니다.",
                example = "1"
        )
        @PositiveOrZero(message = "{validation.pot.display_order.nonnegative}")
        Long displayOrder

) {
}