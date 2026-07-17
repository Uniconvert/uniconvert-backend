package com.uniconvert.backend.domain.user.entity;

import com.uniconvert.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    // 현재 CustomUserDetailsService 빌드 통과용
    // 나중에 local_credential 테이블을 따로 쓰면 이 필드는 제거하거나 구조를 바꾸면 됨
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 255)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 30)
    private UserStatus userStatus = UserStatus.ACTIVE;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "onboarding_step", nullable = false)
    private Integer onboardingStep = 0;

    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    @Column(name = "home_currency_code", nullable = false, length = 3)
    private String homeCurrencyCode = "KRW";

    @Column(name = "local_currency_code", length = 3)
    private String localCurrencyCode;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Seoul";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
    }

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userStatus = UserStatus.ACTIVE;
        this.onboardingStep = 0;
        this.homeCurrencyCode = "KRW";
        this.timezone = "Asia/Seoul";
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.userStatus == null) {
            this.userStatus = UserStatus.ACTIVE;
        }

        if (this.onboardingStep == null) {
            this.onboardingStep = 0;
        }

        if (this.homeCurrencyCode == null) {
            this.homeCurrencyCode = "KRW";
        }

        if (this.timezone == null) {
            this.timezone = "Asia/Seoul";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getOnboardingStep() {
        return onboardingStep;
    }

    public LocalDateTime getOnboardingCompletedAt() {
        return onboardingCompletedAt;
    }

    public String getHomeCurrencyCode() {
        return homeCurrencyCode;
    }

    public String getLocalCurrencyCode() {
        return localCurrencyCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}