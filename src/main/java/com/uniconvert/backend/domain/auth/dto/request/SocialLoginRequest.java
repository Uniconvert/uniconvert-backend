package com.uniconvert.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank(message = "{validation.id_token.required}")
        String idToken
) {
}
