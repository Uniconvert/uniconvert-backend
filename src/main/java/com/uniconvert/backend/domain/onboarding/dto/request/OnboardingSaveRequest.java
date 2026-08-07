package com.uniconvert.backend.domain.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OnboardingSaveRequest(

        @NotBlank(message = "{validation.home_currency.required}")
        @Size(min = 3, max = 3, message = "{validation.home_currency.length3}")
        String homeCurrencyCode,

        @NotBlank(message = "{validation.local_currency.required}")
        @Size(min = 3, max = 3, message = "{validation.local_currency.length3}")
        String localCurrencyCode,

        @NotNull(message = "{validation.budget.required}")
        @PositiveOrZero(message = "{validation.budget.nonnegative}")
        BigDecimal monthlyLimitHome,

        @NotBlank(message = "{validation.timezone.required}")
        @Size(max = 50, message = "{validation.timezone.max50}")
        String timezone,

        @NotBlank(message = "{validation.nickname.required}")
        @Size(max = 20, message = "{validation.nickname.max20}")
        String nickname,

        @Size(max = 500, message = "{validation.profile_image.max500}")
        String imageUrl
) {
}