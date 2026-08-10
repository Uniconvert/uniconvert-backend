package com.uniconvert.backend.domain.user.entity;

import com.uniconvert.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    public static final int ONBOARDING_NOT_STARTED = 0;
    public static final int ONBOARDING_CURRENCY_SELECTED = 1;
    public static final int ONBOARDING_BUDGET_SET = 2;
    public static final int ONBOARDING_TIMEZONE_SET = 3;
    public static final int ONBOARDING_PROFILE_SET = 4;
    public static final int ONBOARDING_COMPLETED = 5;

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

    @Column(name = "profile_image_key", length = 100)
    private String profileImageKey;

    @Column(name = "primary_goal", length = 100)
    private String primaryGoal;

    @Column(name = "onboarding_step", nullable = false)
    private Integer onboardingStep = ONBOARDING_NOT_STARTED;

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

    @Column(name = "email_report_enabled")
    private Boolean emailReportEnabled = false;

    @Column(name = "email_report_send_time")
    private LocalTime emailReportSendTime;

    @Column(name = "email_report_frequency", length = 20)
    private String emailReportFrequency;

    protected User() {
    }

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userStatus = UserStatus.ACTIVE;
        this.onboardingStep = ONBOARDING_NOT_STARTED;
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
            this.onboardingStep = ONBOARDING_NOT_STARTED;
        }

        if (this.homeCurrencyCode == null || this.homeCurrencyCode.isBlank()) {
            this.homeCurrencyCode = "KRW";
        }

        if (this.timezone == null || this.timezone.isBlank()) {
            this.timezone = "Asia/Seoul";
        }

        if (this.emailReportEnabled == null) {
            this.emailReportEnabled = false;
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

    public String getProfileImageKey() {
        return profileImageKey;
    }

    public String getPrimaryGoal() {
        return primaryGoal;
    }

    public Boolean getEmailReportEnabled() { return emailReportEnabled; }

    public LocalTime getEmailReportSendTime() { return emailReportSendTime; }

    public String getEmailReportFrequency() { return emailReportFrequency; }

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

    public boolean isOnboardingCompleted() {
        return this.onboardingCompletedAt != null
                && this.onboardingStep != null
                && this.onboardingStep >= ONBOARDING_COMPLETED;
    }

    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateProfileImageKey(String profileImageKey) {
        this.profileImageKey = normalizeNullableText(profileImageKey);
    }

    public void updatePrimaryGoal(String primaryGoal) {
        this.primaryGoal = normalizeNullableText(primaryGoal);
    }

    public void updateEmailReportSetting(boolean enabled, LocalTime sendTime, String frequency) {
        this.emailReportEnabled = enabled;
        this.emailReportSendTime = enabled ? sendTime : null;
        this.emailReportFrequency = enabled ? frequency : null;
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public void updateHomeCurrencyCode(String homeCurrencyCode) {
        if (homeCurrencyCode != null && !homeCurrencyCode.isBlank()) {
            this.homeCurrencyCode = homeCurrencyCode.toUpperCase();
            this.onboardingStep = Math.max(this.onboardingStep, ONBOARDING_CURRENCY_SELECTED);
        }
    }

    public void updateLocalCurrencyCode(String localCurrencyCode) {
        if (localCurrencyCode != null && !localCurrencyCode.isBlank()) {
            this.localCurrencyCode = localCurrencyCode.toUpperCase();
            this.onboardingStep = Math.max(this.onboardingStep, ONBOARDING_CURRENCY_SELECTED);
        }
    }

    public void updateTimezone(String timezone) {
        if (timezone != null && !timezone.isBlank()) {
            this.timezone = timezone;
            this.onboardingStep = Math.max(this.onboardingStep, ONBOARDING_TIMEZONE_SET);
        }
    }

    public void updateOnboardingInfo(
            String homeCurrencyCode,
            String localCurrencyCode,
            String timezone
    ) {
        updateHomeCurrencyCode(homeCurrencyCode);
        updateLocalCurrencyCode(localCurrencyCode);
        updateTimezone(timezone);
    }

    public void markBudgetSet() {
        this.onboardingStep = Math.max(this.onboardingStep, ONBOARDING_BUDGET_SET);
    }

    public void markProfileSet() {
        this.onboardingStep = Math.max(this.onboardingStep, ONBOARDING_PROFILE_SET);
    }

    public void completeOnboarding() {
        this.onboardingStep = ONBOARDING_COMPLETED;

        if (this.onboardingCompletedAt == null) {
            this.onboardingCompletedAt = LocalDateTime.now();
        }
    }
}