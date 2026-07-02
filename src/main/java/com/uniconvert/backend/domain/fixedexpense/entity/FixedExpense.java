package com.uniconvert.backend.domain.fixedexpense.entity;

import com.uniconvert.backend.domain.category.entity.Category;
import com.uniconvert.backend.domain.fixedexpense.enums.RecurrenceType;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "fixed_expense")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedExpense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fixed_expense_id")
    private Long fixedExpenseId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "original_currency", nullable = false, length = 255)
    private String originalCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 30)
    private RecurrenceType recurrenceType;

    @Column(name = "payment_day")
    private Byte paymentDay;

    @Column
    private Byte weekday;

    @Column(name = "next_scheduled_date", nullable = false)
    private LocalDate nextScheduledDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private SubWallet wallet;

    @Builder
    private FixedExpense(
            Long fixedExpenseId,
            String name,
            BigDecimal originalAmount,
            String originalCurrency,
            RecurrenceType recurrenceType,
            Byte paymentDay,
            Byte weekday,
            LocalDate nextScheduledDate,
            boolean isActive,
            User user,
            Category category,
            SubWallet wallet
    ) {
        this.fixedExpenseId = fixedExpenseId;
        this.name = name;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.recurrenceType = recurrenceType;
        this.paymentDay = paymentDay;
        this.weekday = weekday;
        this.nextScheduledDate = nextScheduledDate;
        this.isActive = isActive;
        this.user = user;
        this.category = category;
        this.wallet = wallet;
    }
}
