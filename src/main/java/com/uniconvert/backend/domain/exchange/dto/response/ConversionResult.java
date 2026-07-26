package com.uniconvert.backend.domain.exchange.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConversionResult(BigDecimal rate, LocalDate rateDate) {
}