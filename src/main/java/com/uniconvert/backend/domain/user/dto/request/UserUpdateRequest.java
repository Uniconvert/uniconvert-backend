package com.uniconvert.backend.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Size(max = 50, message = "닉네임은 50자 이하로 입력해야 합니다.")
        String nickname,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하로 입력해야 합니다.")
        String imageUrl
) {
}