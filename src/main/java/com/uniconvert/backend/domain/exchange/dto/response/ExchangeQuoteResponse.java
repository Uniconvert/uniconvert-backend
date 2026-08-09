// ExchangeQuoteResponse.java — domain/exchange/dto/response
package com.uniconvert.backend.domain.exchange.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeQuoteResponse(
        boolean available,
        String fromCurrency,
        String toCurrency,
        BigDecimal amount,
        BigDecimal appliedRate,
        BigDecimal convertedAmount,
        LocalDate rateDate
) {
    public static ExchangeQuoteResponse of(String fromCurrency, String toCurrency, BigDecimal amount,
                                           BigDecimal appliedRate, BigDecimal convertedAmount, LocalDate rateDate) {
        return new ExchangeQuoteResponse(true, fromCurrency, toCurrency, amount, appliedRate, convertedAmount, rateDate);
    }

    public static ExchangeQuoteResponse unavailable(String fromCurrency, String toCurrency, BigDecimal amount) {
        return new ExchangeQuoteResponse(false, fromCurrency, toCurrency, amount, null, null, null);
    }
}