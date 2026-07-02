package com.uniconvert.backend.domain.user.entity;

import com.uniconvert.backend.domain.user.enums.UserStatus;
import com.uniconvert.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(length = 255)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "home_currency", length = 10)
    private String homeCurrency;

    @Column(name = "onboarding_step", nullable = false)
    private Byte onboardingStep;

    @Column(name = "is_onboarding_completed", nullable = false)
    private boolean isOnboardingCompleted;

    @Column(length = 50, nullable = false)
    private String timezone;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "rejoin_available_at")
    private LocalDateTime rejoinAvailableAt;

    @Builder
    private User(
            Long userId,
            String email,
            String nickname,
            UserStatus status,
            String homeCurrency,
            Byte onboardingStep,
            boolean isOnboardingCompleted,
            String timezone,
            LocalDateTime withdrawnAt,
            LocalDateTime rejoinAvailableAt
    ) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.status = status;
        this.homeCurrency = homeCurrency;
        this.onboardingStep = onboardingStep;
        this.isOnboardingCompleted = isOnboardingCompleted;
        this.timezone = timezone;
        this.withdrawnAt = withdrawnAt;
        this.rejoinAvailableAt = rejoinAvailableAt;
    }
}
