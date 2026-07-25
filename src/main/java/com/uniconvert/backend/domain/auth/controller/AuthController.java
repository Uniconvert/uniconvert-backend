package com.uniconvert.backend.domain.auth.controller;

import com.uniconvert.backend.domain.auth.dto.request.LocalLoginRequest;
import com.uniconvert.backend.domain.auth.dto.request.LocalSignUpRequest;
import com.uniconvert.backend.domain.auth.dto.request.LogoutRequest;
import com.uniconvert.backend.domain.auth.dto.request.TokenReissueRequest;
import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;
import com.uniconvert.backend.domain.auth.service.LocalAuthService;
import com.uniconvert.backend.domain.auth.service.SocialAuthService;
import com.uniconvert.backend.domain.auth.dto.request.SocialLoginRequest;
import com.uniconvert.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증·JWT (회원가입/로그인/이메일 인증/토큰 재발급)")
public class AuthController {

    private final SocialAuthService socialAuthService;

    private final LocalAuthService localAuthService;

    @Operation(summary = "자체 회원가입 | 텐텐")
    @PostMapping("/signup")
    public ApiResponse<LoginResponse> signUp(@Valid @RequestBody LocalSignUpRequest request) {
        return ApiResponse.success(localAuthService.signUp(request));
    }

    @Operation(summary = "자체 로그인 | 텐텐")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LocalLoginRequest request) {
        return ApiResponse.success(localAuthService.login(request));
    }

    @Operation(summary = "구글 소셜 로그인 | 텐텐")
    @PostMapping("/social/google")
    public ApiResponse<LoginResponse> googleLogin(
            @Valid @RequestBody SocialLoginRequest request
    ) {
        return ApiResponse.success(
                socialAuthService.googleLogin(request.idToken())
        );
    }

    @Operation(summary = "Access Token 재발급 | 텐텐")
    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        return ApiResponse.success(localAuthService.reissue(request));
    }

    @Operation(summary = "로그아웃 | 텐텐")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        localAuthService.logout(request);
        return ApiResponse.success(null);
    }
}
