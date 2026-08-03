package com.uniconvert.backend.domain.report.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

// JPQL 집계 쿼리 결과를 바로 받는 용도
public record DailyAmount(LocalDate date, BigDecimal amount) {
}