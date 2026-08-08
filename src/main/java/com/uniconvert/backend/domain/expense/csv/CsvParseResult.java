package com.uniconvert.backend.domain.expense.csv;

import java.util.List;

// transactions: 저장 후보(입금·충전·취소 등은 이미 제외된 상태)
// excludedCount: 필터 조건에 맞지 않아 제외된 행 수 (입금/충전/취소 등)
// invalidRowNumbers: 형식이 깨져 읽지 못한 행 번호 (금액·날짜 파싱 실패 등)
//   — 사유 문구는 로케일에 맞춰 서비스에서 붙인다(파서는 MessageSource를 모른다)
public record CsvParseResult(
        List<ParsedCsvTransaction> transactions,
        int totalRowCount,
        int excludedCount,
        List<Integer> invalidRowNumbers
) {
}
