package com.uniconvert.backend.domain.auth.entity;

import com.uniconvert.backend.domain.auth.enums.TermsType;
import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "terms_agreement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 30)
    private TermsType termsType;

    @Column(name = "terms_version", nullable = false, length = 255)
    private String termsVersion;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private TermsAgreement(
            Long agreementId,
            TermsType termsType,
            String termsVersion,
            boolean isAgreed,
            LocalDateTime agreedAt,
            User user
    ) {
        this.agreementId = agreementId;
        this.termsType = termsType;
        this.termsVersion = termsVersion;
        this.isAgreed = isAgreed;
        this.agreedAt = agreedAt;
        this.user = user;
    }
}
