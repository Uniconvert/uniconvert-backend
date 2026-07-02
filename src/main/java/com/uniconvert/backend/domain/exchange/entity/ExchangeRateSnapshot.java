package com.uniconvert.backend.domain.exchange.entity;

import com.uniconvert.backend.domain.exchange.enums.CollectionSlot;
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
@Table(name = "exchange_rate_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRateSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_rate_snapshot_id")
    private Long exchangeRateSnapshotId;

    @Column(name = "from_currency", nullable = false, length = 255)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 255)
    private String toCurrency;

    @Column(name = "rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_slot", nullable = false, length = 20)
    private CollectionSlot collectionSlot;

    @CreationTimestamp
    @Column(name = "collected_at", nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    @Builder
    private ExchangeRateSnapshot(
            Long exchangeRateSnapshotId,
            String fromCurrency,
            String toCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            CollectionSlot collectionSlot
    ) {
        this.exchangeRateSnapshotId = exchangeRateSnapshotId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.rateDate = rateDate;
        this.collectionSlot = collectionSlot;
    }
}
