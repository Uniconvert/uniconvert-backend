package com.uniconvert.backend.domain.exchange.service;

import com.uniconvert.backend.domain.exchange.client.EcosApiClient;
import com.uniconvert.backend.domain.exchange.client.dto.EcosSearchRow;
import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;
import com.uniconvert.backend.domain.exchange.enums.EcosItemCode;
import com.uniconvert.backend.domain.exchange.repository.DailyExchangeRateRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final DateTimeFormatter ECOS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String HOME_CURRENCY = "KRW";

    private final EcosApiClient ecosApiClient;
    private final DailyExchangeRateRepository dailyExchangeRateRepository;

    @Transactional
    public DailyExchangeRate getCurrentRate(String currencyCode) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 날짜로 이미 저장된 값 있으면 바로 사용 (ECOS 호출 안 함)
        Optional<DailyExchangeRate> todayRate = dailyExchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(currencyCode, HOME_CURRENCY, today);
        if (todayRate.isPresent()) {
            return todayRate.get();
        }

        // 2. 없으면 ECOS 실시간 호출
        Optional<EcosSearchRow> fetched = ecosApiClient.fetchLatestRate(currencyCode);
        if (fetched.isPresent()) {
            return saveFromEcos(currencyCode, fetched.get());
        }

        // 3. 실패 시(8시 이전, 휴장일 등) DB의 가장 최근 값 사용
        return dailyExchangeRateRepository
                .findTopByFromCurrencyAndToCurrencyOrderByRateDateDesc(currencyCode, HOME_CURRENCY)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }
    // 특정 과거 날짜의 환율 조회 (CSV 명세서 등 과거 지출 입력 시 사용)
    @Transactional
    public DailyExchangeRate getRateByDate(String currencyCode, LocalDate targetDate) {
        Optional<DailyExchangeRate> savedRate = dailyExchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(currencyCode, HOME_CURRENCY, targetDate);
        if (savedRate.isPresent()) {
            return savedRate.get();
        }

        // ★ getCurrentRate()는 fetchLatestRate() 호출 → 여기는 fetchRateByDate() 호출로 다름
        Optional<EcosSearchRow> fetched = ecosApiClient.fetchRateByDate(currencyCode, targetDate);
        if (fetched.isPresent()) {
            return saveFromEcos(currencyCode, fetched.get());
        }

        // ★ getCurrentRate()는 실패 시 "가장 최근 값"으로 fallback하지만,
        //   과거 날짜 조회는 엉뚱한 날짜 값을 잘못 적용하면 안 되므로 그냥 예외 처리
        throw new CustomException(ErrorCode.NOT_FOUND);
    }


    private DailyExchangeRate saveFromEcos(String currencyCode, EcosSearchRow row) {
        LocalDate rateDate = LocalDate.parse(row.time(), ECOS_DATE_FORMAT);
        BigDecimal rawValue = new BigDecimal(row.dataValue());

        EcosItemCode itemCode = EcosItemCode.from(currencyCode);
        BigDecimal normalizedRate = rawValue.divide(itemCode.getUnitDivisor(), 4, RoundingMode.HALF_UP);

        // 동시 요청 등으로 이미 저장돼 있으면 재사용, 없으면 신규 저장
        return dailyExchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(currencyCode, HOME_CURRENCY, rateDate)
                .orElseGet(() -> dailyExchangeRateRepository.save(
                        new DailyExchangeRate(currencyCode, HOME_CURRENCY, normalizedRate, rateDate)
                ));
    }
}