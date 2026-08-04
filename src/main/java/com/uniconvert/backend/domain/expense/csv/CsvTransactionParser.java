package com.uniconvert.backend.domain.expense.csv;

import org.apache.commons.csv.CSVParser;

import java.util.List;

public interface CsvTransactionParser {

    // CSV 헤더로 Wise/Monzo 형식을 자동 판별하기 위한 지원 여부 체크
    boolean supports(List<String> headers);

    // 응답의 provider 필드에 그대로 노출되는 이름 (예: "WISE", "MONZO")
    String providerName();

    CsvParseResult parse(CSVParser csvParser);
}
