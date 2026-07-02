package com.uniconvert.backend.domain.wallet.entity;

import com.uniconvert.backend.domain.wallet.enums.MainWalletTransactionType;
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
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "main_wallet_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "main_wallet_transaction_id")
    private Long mainWalletTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MainWalletTransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 255)
    private String memo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_wallet_id", nullable = false)
    private MainWallet mainWallet;

    @Builder
    private MainWalletTransaction(
            Long mainWalletTransactionId,
            MainWalletTransactionType type,
            BigDecimal amount,
            String memo,
            MainWallet mainWallet
    ) {
        this.mainWalletTransactionId = mainWalletTransactionId;
        this.type = type;
        this.amount = amount;
        this.memo = memo;
        this.mainWallet = mainWallet;
    }
}
