// ExchangeQuoteResponse.java — domain/exchange/dto/response
package com.uniconvert.backend.domain.exchange.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeQuoteResponse(
        String fromCurrency,
        String toCurrency,
        BigDecimal amount,
        BigDecimal appliedRate,
        BigDecimal convertedAmount,
        LocalDate rateDate
) {
}