package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;

public record CategoryAmount(Long categoryId, BigDecimal amount) {
}