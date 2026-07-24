package com.uniconvert.backend.domain.exchange.entity;

import com.uniconvert.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "daily_exchange_rate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_currency_pair_date",
                columnNames = {"from_currency", "to_currency", "rate_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyExchangeRate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    public DailyExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, LocalDate rateDate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.rateDate = rateDate;
    }
}