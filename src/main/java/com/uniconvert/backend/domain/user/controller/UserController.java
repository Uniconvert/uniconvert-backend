package com.uniconvert.backend.domain.user.controller;

import com.uniconvert.backend.domain.user.dto.request.UserUpdateRequest;
import com.uniconvert.backend.domain.user.dto.response.UserMeResponse;
import com.uniconvert.backend.domain.user.service.UserService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User & Onboarding", description = "사용자 정보 및 온보딩 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회 | 텐텐", description = "로그인한 사용자의 기본 정보와 온보딩 상태를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                userService.getMyInfo(userDetails.getUserId())
        );
    }

    @Operation(summary = "내 정보 수정 | 텐텐", description = "로그인한 사용자의 닉네임과 프로필 이미지를 수정합니다.")
    @PatchMapping("/me")
    public ApiResponse<UserMeResponse> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.success(
                userService.updateMyInfo(userDetails.getUserId(), request)
        );
    }
}