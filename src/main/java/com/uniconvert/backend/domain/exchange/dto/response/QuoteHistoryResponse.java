package com.uniconvert.backend.domain.exchange.dto.response;

import com.uniconvert.backend.domain.exchange.entity.QuoteHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuoteHistoryResponse(
        Long id,
        String fromCurrency,
        String toCurrency,
        BigDecimal amount,
        BigDecimal convertedAmount,
        BigDecimal appliedRate,
        LocalDateTime createdAt
) {
    public static QuoteHistoryResponse from(QuoteHistory entity) {
        return new QuoteHistoryResponse(
                entity.getId(),
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getAmount(),
                entity.getConvertedAmount(),
                entity.getAppliedRate(),
                entity.getCreatedAt()
        );
    }
}