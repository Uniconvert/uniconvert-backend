package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;

public record SignUpResult(
        LoginResponse loginResponse,
        boolean verificationEmailResent
) {
    public static SignUpResult created(LoginResponse loginResponse) {
        return new SignUpResult(loginResponse, false);
    }

    public static SignUpResult reusedUnverified(LoginResponse loginResponse) {
        return new SignUpResult(loginResponse, true);
    }
}
