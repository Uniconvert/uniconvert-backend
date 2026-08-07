package com.uniconvert.backend.domain.auth.entity;

import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "local_credential")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_credential_id")
    private Long localCredentialId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "failed_login_count", nullable = false)
    private Byte failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    public int increaseFailedLoginCount() {
        int currentCount = failedLoginCount == null ? 0 : failedLoginCount;
        int nextCount = Math.min(currentCount + 1, 3);
        this.failedLoginCount = (byte) nextCount;
        return nextCount;
    }

    public void resetFailedLoginCount() {
        this.failedLoginCount = 0;
    }

    @Builder
    private LocalCredential(
            Long localCredentialId,
            String passwordHash,
            boolean isEmailVerified,
            Byte failedLoginCount,
            LocalDateTime lockedUntil,
            LocalDateTime passwordChangedAt,
            User user
    ) {
        this.localCredentialId = localCredentialId;
        this.passwordHash = passwordHash;
        this.isEmailVerified = isEmailVerified;
        this.failedLoginCount = failedLoginCount == null ? 0 : failedLoginCount;
        this.lockedUntil = lockedUntil;
        this.passwordChangedAt = passwordChangedAt;
        this.user = user;
    }
}
