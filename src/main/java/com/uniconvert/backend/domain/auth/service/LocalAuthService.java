package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.dto.request.LocalLoginRequest;
import com.uniconvert.backend.domain.auth.dto.request.LocalSignUpRequest;
import com.uniconvert.backend.domain.auth.dto.request.LogoutRequest;
import com.uniconvert.backend.domain.auth.dto.request.TokenReissueRequest;
import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;
import com.uniconvert.backend.domain.auth.entity.LocalCredential;
import com.uniconvert.backend.domain.auth.entity.RefreshToken;
import com.uniconvert.backend.domain.auth.repository.LocalCredentialRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.enums.UserStatus;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public SignUpResult signUp(LocalSignUpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        User existingUser = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingUser != null) {
            LocalCredential existingCredential = localCredentialRepository.findByUser(existingUser)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

            if (existingCredential.isEmailVerified()) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_REGISTERED);
            }

            emailVerificationService.resend(existingUser);

            LoginResponse response = new LoginResponse(
                    existingUser.getUserId(),
                    existingUser.getEmail(),
                    existingUser.getNickname(),
                    null,
                    null
            );
            return SignUpResult.reusedUnverified(response);
        }

        String normalizedNickname = request.nickname().trim();

        User user = new User(
                normalizedEmail,
                null,
                normalizedNickname
        );

        User savedUser = userRepository.save(user);

        LocalCredential credential = LocalCredential.builder()
                .user(savedUser)
                .passwordHash(passwordEncoder.encode(request.password()))
                .isEmailVerified(false)
                .failedLoginCount((byte) 0)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        localCredentialRepository.save(credential);
        emailVerificationService.issueInitial(savedUser);

        String accessToken = tokenService.issueAccessToken(savedUser);
        String refreshToken = tokenService.issueRefreshToken(savedUser);

        LoginResponse response = new LoginResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                accessToken,
                refreshToken
        );
        return SignUpResult.created(response);
    }

    @Transactional
    public LoginResponse login(LocalLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmailAndUserStatus(normalizedEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_INVALID_CREDENTIALS));

        LocalCredential credential = localCredentialRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_INVALID_CREDENTIALS));

        if (!credential.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            int failedCount = loginAttemptService.recordFailure(credential.getLocalCredentialId());

            if (failedCount >= 3) {
                throw new CustomException(ErrorCode.LOGIN_PASSWORD_FAILURE_LIMIT);
            }

            throw new CustomException(ErrorCode.LOGIN_INVALID_CREDENTIALS);
        }

        credential.resetFailedLoginCount();

        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(user);

        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public LoginResponse reissue(TokenReissueRequest request) {
        RefreshToken savedRefreshToken = tokenService.validateRefreshToken(request.refreshToken());
        User user = savedRefreshToken.getUser();

        savedRefreshToken.revoke();

        String newAccessToken = tokenService.issueAccessToken(user);
        String newRefreshToken = tokenService.issueRefreshToken(user);

        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                newAccessToken,
                newRefreshToken
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {
        tokenService.revokeRefreshToken(request.refreshToken());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
