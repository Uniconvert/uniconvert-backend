package com.uniconvert.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(

        @NotBlank(message = "{validation.refresh_token.required}")
        String refreshToken
) {
}