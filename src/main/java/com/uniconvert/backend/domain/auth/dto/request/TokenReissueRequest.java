package com.uniconvert.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(

        @NotBlank
        String refreshToken
) {
}