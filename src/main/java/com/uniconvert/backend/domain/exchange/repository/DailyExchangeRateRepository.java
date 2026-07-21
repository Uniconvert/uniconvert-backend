package com.uniconvert.backend.domain.exchange.repository;

import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyExchangeRateRepository extends JpaRepository<DailyExchangeRate, Long> {

    Optional<DailyExchangeRate> findByFromCurrencyAndToCurrencyAndRateDate(
            String fromCurrency, String toCurrency, LocalDate rateDate);

    // 8시 이전·휴장일 fallback용 — 통화쌍의 가장 최근 저장값
    Optional<DailyExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByRateDateDesc(
            String fromCurrency, String toCurrency);
}