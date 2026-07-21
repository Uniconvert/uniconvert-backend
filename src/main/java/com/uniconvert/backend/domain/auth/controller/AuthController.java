package com.uniconvert.backend.domain.auth.controller;

import com.uniconvert.backend.domain.auth.dto.request.LocalLoginRequest;
import com.uniconvert.backend.domain.auth.dto.request.LocalSignUpRequest;
import com.uniconvert.backend.domain.auth.dto.request.LogoutRequest;
import com.uniconvert.backend.domain.auth.dto.request.TokenReissueRequest;
import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;
import com.uniconvert.backend.domain.auth.service.LocalAuthService;
import com.uniconvert.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final LocalAuthService localAuthService;

    @PostMapping("/signup")
    public ApiResponse<LoginResponse> signUp(@Valid @RequestBody LocalSignUpRequest request) {
        return ApiResponse.success(localAuthService.signUp(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LocalLoginRequest request) {
        return ApiResponse.success(localAuthService.login(request));
    }

    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        return ApiResponse.success(localAuthService.reissue(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        localAuthService.logout(request);
        return ApiResponse.success(null);
    }
}
