package com.uniconvert.backend.domain.expense.csv;

import java.util.List;

// transactions: 저장 후보(입금·충전·취소 등은 이미 제외된 상태)
// excludedCount: 필터 조건에 맞지 않아 제외된 행 수 (입금/충전/취소 등)
public record CsvParseResult(
        List<ParsedCsvTransaction> transactions,
        int totalRowCount,
        int excludedCount
) {
}
