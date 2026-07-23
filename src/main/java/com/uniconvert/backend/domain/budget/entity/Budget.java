package com.uniconvert.backend.domain.budget.entity;

import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(
        name = "budget",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_user_year_month",
                        columnNames = {"user_id", "year_month"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long budgetId;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "monthly_limit_home", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimitHome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Budget(User user, String yearMonth, BigDecimal monthlyLimitHome) {
        this.user = user;
        this.yearMonth = yearMonth;
        this.monthlyLimitHome = monthlyLimitHome;
    }

    public Long getId() {
        return budgetId;
    }

    public void updateMonthlyLimitHome(BigDecimal monthlyLimitHome) {
        this.monthlyLimitHome = monthlyLimitHome;
    }

    public void updateAmount(BigDecimal monthlyLimitHome) {
        this.monthlyLimitHome = monthlyLimitHome;
    }
}