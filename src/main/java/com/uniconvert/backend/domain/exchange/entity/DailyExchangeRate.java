package com.uniconvert.backend.domain.exchange.entity;

import com.uniconvert.backend.domain.exchange.enums.DailyRateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "daily_exchange_rate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_exchange_rate_id")
    private Long dailyExchangeRateId;

    @Column(name = "from_currency", nullable = false, length = 255)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 255)
    private String toCurrency;

    @Column(name = "average_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageRate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "sample_count", nullable = false)
    private Byte sampleCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DailyRateStatus status = DailyRateStatus.PRELIMINARY;

    @CreationTimestamp
    @Column(name = "last_collected_at", nullable = false, updatable = false)
    private LocalDateTime lastCollectedAt;

    @Builder
    private DailyExchangeRate(
            Long dailyExchangeRateId,
            String fromCurrency,
            String toCurrency,
            BigDecimal averageRate,
            LocalDate rateDate,
            Byte sampleCount,
            DailyRateStatus status,
            LocalDateTime lastCollectedAt
    ) {
        this.dailyExchangeRateId = dailyExchangeRateId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.averageRate = averageRate;
        this.rateDate = rateDate;
        this.sampleCount = sampleCount;
        this.status = status;
        this.lastCollectedAt = lastCollectedAt;
    }
}
