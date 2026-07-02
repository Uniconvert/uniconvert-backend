package com.uniconvert.backend.domain.wallet.entity;

import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "main_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainWallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "main_wallet_id")
    private Long mainWalletId;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder
    private MainWallet(Long mainWalletId, BigDecimal currentBalance, User user) {
        this.mainWalletId = mainWalletId;
        this.currentBalance = currentBalance == null ? BigDecimal.ZERO : currentBalance;
        this.user = user;
    }
}
