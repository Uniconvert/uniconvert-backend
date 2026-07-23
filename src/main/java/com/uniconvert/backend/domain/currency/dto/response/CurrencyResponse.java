package com.uniconvert.backend.domain.currency.dto.response;

public record CurrencyResponse(
        String code,
        String koreanName,
        String englishName,
        String symbol
) {
}