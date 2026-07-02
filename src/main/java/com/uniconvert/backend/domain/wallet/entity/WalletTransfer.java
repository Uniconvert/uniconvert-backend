package com.uniconvert.backend.domain.wallet.entity;

import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "wallet_transfer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_transfer_id")
    private Long walletTransferId;

    @Column(name = "from_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal fromAmount;

    @Column(name = "from_currency", nullable = false, length = 255)
    private String fromCurrency;

    @Column(name = "to_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal toAmount;

    @Column(name = "to_currency", nullable = false, length = 255)
    private String toCurrency;

    @Column(name = "applied_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedRate;

    @Column(name = "is_fixed_rate_enabled", nullable = false)
    private boolean isFixedRateEnabled;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;

    @Column(length = 255)
    private String memo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id", nullable = false)
    private MainWallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id", nullable = false)
    private SubWallet toWallet;

    @Builder
    private WalletTransfer(
            Long walletTransferId,
            BigDecimal fromAmount,
            String fromCurrency,
            BigDecimal toAmount,
            String toCurrency,
            BigDecimal appliedRate,
            boolean isFixedRateEnabled,
            LocalDateTime transferredAt,
            String memo,
            User user,
            MainWallet fromWallet,
            SubWallet toWallet
    ) {
        this.walletTransferId = walletTransferId;
        this.fromAmount = fromAmount;
        this.fromCurrency = fromCurrency;
        this.toAmount = toAmount;
        this.toCurrency = toCurrency;
        this.appliedRate = appliedRate;
        this.isFixedRateEnabled = isFixedRateEnabled;
        this.transferredAt = transferredAt;
        this.memo = memo;
        this.user = user;
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
    }
}
