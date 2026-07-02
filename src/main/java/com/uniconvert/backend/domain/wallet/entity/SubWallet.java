package com.uniconvert.backend.domain.wallet.entity;

import com.uniconvert.backend.domain.category.entity.Category;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.wallet.enums.WalletType;
import com.uniconvert.backend.global.entity.BaseTimeEntity;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "sub_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubWallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_wallet_id")
    private Long subWalletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false, length = 30)
    private WalletType walletType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String currency;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked;

    @Column(name = "is_exchange_rate_fixed", nullable = false)
    private boolean isExchangeRateFixed;

    @Column(name = "fixed_exchange_rate", precision = 19, scale = 4)
    private BigDecimal fixedExchangeRate;

    @Column(name = "fixed_rate_home_currency", length = 255)
    private String fixedRateHomeCurrency;

    @Column(name = "fixed_rate_set_at")
    private LocalDateTime fixedRateSetAt;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    private SubWallet(
            Long subWalletId,
            WalletType walletType,
            String name,
            String currency,
            BigDecimal currentBalance,
            boolean isLocked,
            boolean isExchangeRateFixed,
            BigDecimal fixedExchangeRate,
            String fixedRateHomeCurrency,
            LocalDateTime fixedRateSetAt,
            boolean isArchived,
            User user,
            Category category
    ) {
        this.subWalletId = subWalletId;
        this.walletType = walletType;
        this.name = name;
        this.currency = currency;
        this.currentBalance = currentBalance == null ? BigDecimal.ZERO : currentBalance;
        this.isLocked = isLocked;
        this.isExchangeRateFixed = isExchangeRateFixed;
        this.fixedExchangeRate = fixedExchangeRate;
        this.fixedRateHomeCurrency = fixedRateHomeCurrency;
        this.fixedRateSetAt = fixedRateSetAt;
        this.isArchived = isArchived;
        this.user = user;
        this.category = category;
    }
}
