package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.dto.response.EmailVerificationConfirmResponse;
import com.uniconvert.backend.domain.auth.dto.response.EmailVerificationResponse;
import com.uniconvert.backend.domain.auth.entity.EmailVerifyToken;
import com.uniconvert.backend.domain.auth.entity.LocalCredential;
import com.uniconvert.backend.domain.auth.repository.EmailVerifyTokenRepository;
import com.uniconvert.backend.domain.auth.repository.LocalCredentialRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int MAX_RESENDS = 3;
    private static final int TOKEN_VALID_HOURS = 24;

    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final EmailVerifyTokenRepository emailVerifyTokenRepository;
    private final EmailVerificationMailService emailVerificationMailService;

    @Transactional
    public EmailVerificationResponse issueInitial(User user) {
        LocalCredential credential = getCredential(user);
        if (credential.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        TokenValue tokenValue = newTokenValue();
        EmailVerifyToken token = emailVerifyTokenRepository.findByUser(user)
                .orElseGet(() -> new EmailVerifyToken(user, tokenValue.hash(), tokenValue.expiresAt()));

        if (token.getEmailVerifyTokenId() != null) {
            token.rotateInitial(tokenValue.hash(), tokenValue.expiresAt());
        }

        emailVerifyTokenRepository.save(token);
        emailVerificationMailService.sendVerificationMail(user.getEmail(), tokenValue.rawToken());

        return toResponse(token);
    }

    @Transactional
    public EmailVerificationResponse resend(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_TARGET_NOT_FOUND));

        return resend(user);
    }

    @Transactional
    public EmailVerificationResponse resend(User user) {
        LocalCredential credential = getCredential(user);
        if (credential.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerifyToken token = emailVerifyTokenRepository.findByUser(user)
                .orElse(null);

        if (token != null && token.getResendCount() >= MAX_RESENDS) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED);
        }

        TokenValue tokenValue = newTokenValue();
        if (token == null) {
            token = new EmailVerifyToken(user, tokenValue.hash(), tokenValue.expiresAt());
            // 기존 미인증 레코드에 토큰 행이 없던 경우도 '재발송' 요청이므로 1회로 계산한다.
            token.rotateForResend(tokenValue.hash(), tokenValue.expiresAt());
        } else {
            token.rotateForResend(tokenValue.hash(), tokenValue.expiresAt());
        }

        emailVerifyTokenRepository.save(token);
        emailVerificationMailService.sendVerificationMail(user.getEmail(), tokenValue.rawToken());

        return toResponse(token);
    }

    @Transactional
    public EmailVerificationConfirmResponse confirm(String rawToken) {
        String tokenHash = sha256(rawToken);
        EmailVerifyToken token = emailVerifyTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        if (token.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
        }

        LocalCredential credential = getCredential(token.getUser());
        credential.verifyEmail();

        return new EmailVerificationConfirmResponse(true);
    }

    private LocalCredential getCredential(User user) {
        return localCredentialRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }

    private EmailVerificationResponse toResponse(EmailVerifyToken token) {
        int remaining = Math.max(0, MAX_RESENDS - token.getResendCount());
        return new EmailVerificationResponse(token.getExpiresAt(), token.getResendCount(), remaining);
    }

    private TokenValue newTokenValue() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new TokenValue(
                rawToken,
                sha256(rawToken),
                LocalDateTime.now().plusHours(TOKEN_VALID_HOURS)
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record TokenValue(String rawToken, String hash, LocalDateTime expiresAt) {
    }
}
