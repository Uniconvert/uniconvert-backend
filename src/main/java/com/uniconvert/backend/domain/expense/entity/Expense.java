package com.uniconvert.backend.domain.expense.entity;
import com.uniconvert.backend.global.entity.BaseTimeEntity;

import com.uniconvert.backend.domain.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense")
public class Expense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "original_currency", nullable = false, length = 3)
    private String originalCurrency;

    @Column(name = "applied_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_source", nullable = false, length = 30)
    private RateSource rateSource;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "converted_amount_home", nullable = false, precision = 19, scale = 4)
    private BigDecimal convertedAmountHome;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(name = "merchant_name_normalized", length = 255)
    private String merchantNameNormalized;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "spent_at", nullable = false)
    private LocalDateTime spentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Pot 도메인(텐텐) Entity가 아직 없어 Long으로 직접 보유. Pot Entity 생기면 @ManyToOne으로 전환 예정
    @Column(name = "pot_id")
    private Long potId;

    // CategoryType enum 참조. DB FK 아님, CategoryType.isValid()로 검증
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Expense() {
    }

    public Expense(User user, BigDecimal originalAmount, String originalCurrency,
                   BigDecimal appliedRate, RateSource rateSource, LocalDate rateDate,
                   BigDecimal convertedAmountHome, String merchantName, String merchantNameNormalized,
                   String memo, LocalDateTime spentAt, Long potId, Long categoryId) {
        this.user = user;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.appliedRate = appliedRate;
        this.rateSource = rateSource;
        this.rateDate = rateDate;
        this.convertedAmountHome = convertedAmountHome;
        this.merchantName = merchantName;
        this.merchantNameNormalized = merchantNameNormalized;
        this.memo = memo;
        this.spentAt = spentAt;
        this.potId = potId;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public RateSource getRateSource() {
        return rateSource;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public BigDecimal getConvertedAmountHome() {
        return convertedAmountHome;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantNameNormalized() {
        return merchantNameNormalized;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getSpentAt() {
        return spentAt;
    }

    public User getUser() {
        return user;
    }

    public Long getPotId() {
        return potId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void updateDetails(BigDecimal originalAmount, String originalCurrency, String merchantName,
                              String memo, LocalDateTime spentAt, Long potId, Long categoryId) {
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.merchantName = merchantName;
        this.memo = memo;
        this.spentAt = spentAt;
        this.potId = potId;
        this.categoryId = categoryId;
    }

    // 환율 재계산이 필요한 수정(날짜·통화 변경)에서만 호출
    public void updateRateInfo(BigDecimal appliedRate, RateSource rateSource,
                               LocalDate rateDate, BigDecimal convertedAmountHome) {
        this.appliedRate = appliedRate;
        this.rateSource = rateSource;
        this.rateDate = rateDate;
        this.convertedAmountHome = convertedAmountHome;
    }

    // 소프트 삭제 — 실제 row는 남기고 deleted_at만 채움
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}