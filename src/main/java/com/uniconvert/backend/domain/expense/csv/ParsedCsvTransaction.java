package com.uniconvert.backend.domain.expense.csv;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// CSV 원본 행 하나를 공통 형식(날짜·가맹점·금액·통화·카테고리)으로 변환한 결과.
// rowNumber는 헤더를 제외한 데이터 행 기준 1부터 시작하는 번호(응답의 에러 상세에 사용).
public record ParsedCsvTransaction(
        int rowNumber,
        LocalDateTime spentAt,
        String merchantName,
        BigDecimal originalAmount,
        String originalCurrency,
        Long categoryId
) {
}
