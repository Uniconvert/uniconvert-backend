package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;
import com.uniconvert.backend.domain.auth.entity.RefreshToken;
import com.uniconvert.backend.domain.auth.repository.RefreshTokenRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String issueAccessToken(User user) {
        return jwtTokenProvider.createAccessToken(user.getUserId());
    }

    @Transactional
    public LoginResponse issueLoginResponse(User user) {
        String accessToken = issueAccessToken(user);
        String refreshToken = issueRefreshToken(user);

        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.isOnboardingCompleted(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public String issueRefreshToken(User user) {
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        String tokenHash = hashToken(refreshToken);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(jwtTokenProvider.getExpiration(refreshToken)),
                ZoneId.systemDefault()
        );

        refreshTokenRepository.deleteByUser(user);

        RefreshToken savedToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(savedToken);

        return refreshToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        }

        String tokenHash = hashToken(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("저장되지 않은 refresh token입니다."));

        if (savedToken.isExpired()) {
            throw new IllegalArgumentException("만료된 refresh token입니다.");
        }

        return savedToken;
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        RefreshToken savedToken = validateRefreshToken(refreshToken);
        savedToken.revoke();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 해시 생성에 실패했습니다.", e);
        }
    }
}