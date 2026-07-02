package com.uniconvert.backend.domain.expense.entity;

import com.uniconvert.backend.domain.category.entity.Category;
import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;
import com.uniconvert.backend.domain.expense.enums.ExpenseRateMode;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.wallet.entity.SubWallet;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "expense")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "original_currency", nullable = false, length = 255)
    private String originalCurrency;

    @Column(name = "applied_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_source", nullable = false, length = 30)
    private ExpenseRateMode rateSource = ExpenseRateMode.DAILY_AVERAGE;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "converted_amount_home", nullable = false, precision = 19, scale = 4)
    private BigDecimal convertedAmountHome;

    @Column(name = "home_currency_at", nullable = false, length = 255)
    private String homeCurrencyAt;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(name = "merchant_name_normalized", length = 255)
    private String merchantNameNormalized;

    @Column(length = 255)
    private String memo;

    @Column(name = "spent_at", nullable = false)
    private LocalDateTime spentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private SubWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_exchange_rate_id", nullable = false)
    private DailyExchangeRate dailyExchangeRate;

    @Builder
    private Expense(
            Long expenseId,
            BigDecimal originalAmount,
            String originalCurrency,
            BigDecimal appliedRate,
            ExpenseRateMode rateSource,
            LocalDate rateDate,
            BigDecimal convertedAmountHome,
            String homeCurrencyAt,
            String merchantName,
            String merchantNameNormalized,
            String memo,
            LocalDateTime spentAt,
            User user,
            Category category,
            SubWallet wallet,
            DailyExchangeRate dailyExchangeRate
    ) {
        this.expenseId = expenseId;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.appliedRate = appliedRate;
        this.rateSource = rateSource;
        this.rateDate = rateDate;
        this.convertedAmountHome = convertedAmountHome;
        this.homeCurrencyAt = homeCurrencyAt;
        this.merchantName = merchantName;
        this.merchantNameNormalized = merchantNameNormalized;
        this.memo = memo;
        this.spentAt = spentAt;
        this.user = user;
        this.category = category;
        this.wallet = wallet;
        this.dailyExchangeRate = dailyExchangeRate;
    }
}
