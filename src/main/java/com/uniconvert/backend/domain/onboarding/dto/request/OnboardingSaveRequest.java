package com.uniconvert.backend.domain.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OnboardingSaveRequest(

        @NotBlank(message = "기준 통화를 선택해야 합니다.")
        @Size(min = 3, max = 3, message = "기준 통화 코드는 3자리여야 합니다.")
        String homeCurrencyCode,

        @NotBlank(message = "현지 통화를 선택해야 합니다.")
        @Size(min = 3, max = 3, message = "현지 통화 코드는 3자리여야 합니다.")
        String localCurrencyCode,

        @NotNull(message = "월 예산을 입력해야 합니다.")
        @PositiveOrZero(message = "월 예산은 0 이상이어야 합니다.")
        BigDecimal monthlyLimitHome,

        @NotBlank(message = "시간대를 선택해야 합니다.")
        @Size(max = 50, message = "시간대는 50자 이하로 입력해야 합니다.")
        String timezone,

        @NotBlank(message = "닉네임을 입력해야 합니다.")
        @Size(max = 50, message = "닉네임은 50자 이하로 입력해야 합니다.")
        String nickname,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하로 입력해야 합니다.")
        String imageUrl
) {
}