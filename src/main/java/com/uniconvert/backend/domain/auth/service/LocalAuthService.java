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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse signUp(LocalSignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = new User(
                request.email(),
                null,
                request.nickname()
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

        String accessToken = tokenService.issueAccessToken(savedUser);
        String refreshToken = tokenService.issueRefreshToken(savedUser);

        return new LoginResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public LoginResponse login(LocalLoginRequest request) {
        User user = userRepository.findByEmailAndUserStatus(request.email(), UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        LocalCredential credential = localCredentialRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

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
}