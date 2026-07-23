package com.uniconvert.backend.domain.onboarding.controller;

import com.uniconvert.backend.domain.onboarding.dto.request.OnboardingSaveRequest;
import com.uniconvert.backend.domain.onboarding.dto.response.OnboardingResponse;
import com.uniconvert.backend.domain.onboarding.service.OnboardingService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
@Tag(name = "User & Onboarding", description = "사용자 정보 및 온보딩 관련 API")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 저장 | 텐텐", description = "현지 통화, 기준 통화, 월 예산, 시간대, 프로필 정보를 저장하고 온보딩을 완료 처리합니다.")
    @PostMapping
    public ApiResponse<OnboardingResponse> saveOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingSaveRequest request
    ) {
        return ApiResponse.success(
                onboardingService.saveOnboarding(userDetails.getUserId(), request)
        );
    }

    @Operation(summary = "내 온보딩 정보 조회 | 텐텐", description = "로그인한 사용자의 온보딩 정보와 이번 달 예산 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<OnboardingResponse> getMyOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                onboardingService.getMyOnboarding(userDetails.getUserId())
        );
    }
}