// ExchangeRateResponse.java — domain/exchange/dto/response
package com.uniconvert.backend.domain.exchange.dto.response;

import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        LocalDate rateDate
) {
    public static ExchangeRateResponse from(DailyExchangeRate entity) {
        return new ExchangeRateResponse(
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getRate(),
                entity.getRateDate()
        );
    }
}