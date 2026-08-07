package com.uniconvert.backend.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Size(max = 20, message = "{validation.nickname.max20}")
        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "{validation.nickname.required}")
        String nickname,

        @Size(max = 500, message = "{validation.profile_image.max500}")
        String imageUrl
) {
}