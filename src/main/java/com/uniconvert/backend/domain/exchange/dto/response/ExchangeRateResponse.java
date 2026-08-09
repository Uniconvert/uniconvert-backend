// ExchangeRateResponse.java — domain/exchange/dto/response
package com.uniconvert.backend.domain.exchange.dto.response;

import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(
        boolean available,
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        LocalDate rateDate,
        BigDecimal changeRate,
        LocalDate comparedDate
) {
    public static ExchangeRateResponse from(DailyExchangeRate entity) {
        return of(entity, null, null);
    }

    public static ExchangeRateResponse of(DailyExchangeRate entity,
                                   BigDecimal changeRate,
                                   LocalDate comparedDate) {
        return new ExchangeRateResponse(
                true,
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getRate(),
                entity.getRateDate(),
                changeRate,
                comparedDate
        );
    }
    public static ExchangeRateResponse unavailable(String fromCurrency, String toCurrency) {
        return new ExchangeRateResponse(false, fromCurrency, toCurrency, null, null, null, null);
    }
}