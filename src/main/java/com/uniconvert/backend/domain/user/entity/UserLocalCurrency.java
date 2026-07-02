package com.uniconvert.backend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "user_local_currency")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLocalCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_local_currency_id")
    private Long userLocalCurrencyId;

    @Column(name = "currency_code", nullable = false, length = 255)
    private String currencyCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private UserLocalCurrency(Long userLocalCurrencyId, String currencyCode, User user) {
        this.userLocalCurrencyId = userLocalCurrencyId;
        this.currencyCode = currencyCode;
        this.user = user;
    }
}
