package com.uniconvert.backend.domain.expense.csv;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;

public interface CsvTransactionParser {

    // CSV 헤더로 Wise/Monzo 형식을 자동 판별하기 위한 지원 여부 체크
    boolean supports(List<String> headers);

    // 응답의 provider 필드에 그대로 노출되는 이름 (예: "WISE", "MONZO")
    String providerName();

    // 행 하나를 공통 형식으로 변환. 지출성 거래가 아니면 null (excludedCount로 집계)
    ParsedCsvTransaction parseRow(CSVRecord record, int rowNumber);

    // 행 단위 파싱 실패는 요청 전체를 실패시키지 않고 errors로 모은다.
    // (Monzo Emoji 열이 깨져 열이 밀리면 Amount에 통화코드가 들어와 BigDecimal이 터지는 사례가 있었다)
    default CsvParseResult parse(CSVParser csvParser) {
        List<ParsedCsvTransaction> transactions = new ArrayList<>();
        List<Integer> invalidRowNumbers = new ArrayList<>();
        int total = 0;
        int excluded = 0;

        for (CSVRecord record : csvParser) {
            total++;
            try {
                ParsedCsvTransaction transaction = parseRow(record, total);
                if (transaction == null) {
                    excluded++;
                } else {
                    transactions.add(transaction);
                }
            } catch (RuntimeException e) {
                invalidRowNumbers.add(total);
            }
        }

        return new CsvParseResult(transactions, total, excluded, invalidRowNumbers);
    }
}
