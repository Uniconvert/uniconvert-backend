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
import com.uniconvert.backend.domain.exchange.dto.response.ExchangeRateResponse;
import com.uniconvert.backend.domain.exchange.dto.response.ConversionResult;


import com.uniconvert.backend.domain.exchange.entity.QuoteHistory;
import com.uniconvert.backend.domain.exchange.repository.QuoteHistoryRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final QuoteHistoryRepository quoteHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExchangeRateResponse getCurrentRateWithChange(String currencyCode) {
        DailyExchangeRate entity = getCurrentRate(currencyCode);

        ChangeInfo change = calculateChange(
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getRateDate(),
                entity.getRate()
        );

        return ExchangeRateResponse.of(entity, change.changeRate(), change.comparedDate());
    }

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

    @Transactional
    public ConversionResult getConversionRate(String originalCurrency, String homeCurrency, LocalDate targetDate) {

        if (originalCurrency.equals(homeCurrency)) {
            return new ConversionResult(BigDecimal.ONE, targetDate);
        }

        if (HOME_CURRENCY.equals(homeCurrency)) {
            DailyExchangeRate rate = getRateByDate(originalCurrency, targetDate);
            return new ConversionResult(rate.getRate(), rate.getRateDate());
        }

        BigDecimal originalToKrwRate = HOME_CURRENCY.equals(originalCurrency)
                ? BigDecimal.ONE
                : getRateByDate(originalCurrency, targetDate).getRate();

        DailyExchangeRate homeToKrw = getRateByDate(homeCurrency, targetDate);

        BigDecimal crossRate = originalToKrwRate
                .divide(homeToKrw.getRate(), 6, RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP);

        return new ConversionResult(crossRate, targetDate);
    }

    private ChangeInfo calculateChange(String from, String to, LocalDate baseDate, BigDecimal baseRate) {
        return dailyExchangeRateRepository
                .findTopByFromCurrencyAndToCurrencyAndRateDateLessThanOrderByRateDateDesc(from, to, baseDate)
                // 0 또는 null이면 나눗셈 불가 → 비교 포기
                .filter(prev -> prev.getRate() != null
                        && prev.getRate().compareTo(BigDecimal.ZERO) > 0)
                .map(prev -> new ChangeInfo(
                        baseRate.subtract(prev.getRate())
                                .divide(prev.getRate(), 6, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP),   // 소수점 2자리 (예: 0.80)
                        prev.getRateDate()
                ))
                // 비교 대상 없음(첫 수집일 등) → null 반환, 프론트에서 배지 숨김
                .orElse(new ChangeInfo(null, null));
    }

    private record ChangeInfo(BigDecimal changeRate, LocalDate comparedDate) {}

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
    // 계산 내역 저장 (quote 계산할 때마다 호출)
    @Transactional
    public void saveQuoteHistory(Long userId, String fromCurrency, String toCurrency,
                                 BigDecimal amount, BigDecimal convertedAmount, BigDecimal appliedRate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        quoteHistoryRepository.save(
                new QuoteHistory(user, fromCurrency, toCurrency, amount, convertedAmount, appliedRate)
        );
    }

    // 최근 계산 내역 조회 (페이지네이션)
    @Transactional(readOnly = true)
    public Page<QuoteHistory> getQuoteHistory(Long userId, Pageable pageable) {
        return quoteHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }
}