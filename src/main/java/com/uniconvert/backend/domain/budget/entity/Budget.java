package com.uniconvert.backend.domain.budget.entity;

import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "budget")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "monthly_limit_home", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyLimitHome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Budget(String yearMonth, BigDecimal monthlyLimitHome, User user) {
        this.yearMonth = yearMonth;
        this.monthlyLimitHome = monthlyLimitHome;
        this.user = user;
    }

    // PUT /budgets/{yearMonth} — upsert 시 이미 존재하면 값만 갱신
    public void updateAmount(BigDecimal monthlyLimitHome) {
        this.monthlyLimitHome = monthlyLimitHome;
    }
}